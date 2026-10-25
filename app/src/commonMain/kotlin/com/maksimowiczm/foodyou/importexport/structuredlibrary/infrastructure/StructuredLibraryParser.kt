package com.maksimowiczm.foodyou.importexport.structuredlibrary.infrastructure

import com.maksimowiczm.foodyou.common.domain.food.*
import com.maksimowiczm.foodyou.common.infrastructure.room.toValidatedEntityNutrients
import com.maksimowiczm.foodyou.importexport.structuredlibrary.domain.*
import io.ktor.util.Digest
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.*

/** Bounded external-input boundary. Diagnostics never contain decoder messages or source values. */
class StructuredLibraryParser {
    private val json = Json { encodeDefaults = true; explicitNulls = true }

    suspend fun parse(bytes: ByteArray): ValidatedLibrary {
        if (bytes.size > MAX_BYTES) fail(LibraryErrorCode.FILE_TOO_LARGE)
        val bundle = try {
            val text = bytes.decodeToString(throwOnInvalidSequence = true)
            UniqueJsonKeys(text).validate(LibraryBundle.serializer().descriptor)
            json.decodeFromString<LibraryBundle>(text)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (invalid: LibraryImportException) { throw invalid }
        catch (_: Exception) { fail(LibraryErrorCode.MALFORMED) }
        if (bundle.format != "foodus-library" || bundle.formatVersion != 1) fail(LibraryErrorCode.VERSION)
        bounded(bundle.namespace,128,required=true)
        if (bundle.products.size.toLong()+bundle.recipes.size+bundle.meals.size > MAX_RECORDS) fail(LibraryErrorCode.LIMIT)
        val normalized = bundle.copy(products=bundle.products.map { it.copy(name=it.name.trim()) },
            recipes=bundle.recipes.map { it.copy(name=it.name.trim(), components=it.components.map { c -> c.copy(name=c.name.trim()) }) },
            meals=bundle.meals.map { it.copy(name=it.name.trim(), items=it.items.map { item -> item.copy(name=item.name.trim()) }) })
        val keys = mutableSetOf<LibraryKey>()
        fun identity(kind: DefinitionKind, index: Int, id: String, name: String) {
            bounded(id,256,true,kind,index); bounded(name,512,true,kind,index)
            if (!keys.add(LibraryKey(kind,id))) fail(LibraryErrorCode.IDENTITY,kind,index)
        }
        normalized.products.forEachIndexed { i, p -> identity(DefinitionKind.PRODUCT,i,p.externalId,p.name) }
        normalized.recipes.forEachIndexed { i, r -> identity(DefinitionKind.RECIPE,i,r.externalId,r.name) }
        normalized.meals.forEachIndexed { i, m -> identity(DefinitionKind.MEAL,i,m.externalId,m.name) }
        var componentCount = 0L
        normalized.products.forEachIndexed { i,p ->
            currentCoroutineContext().ensureActive()
            val k=DefinitionKind.PRODUCT
            facts(p.nutrition,k,i); portion(p.reference,k,i); metadata(p.metadata,k,i)
            bounded(p.brand,512,kind=k,index=i); bounded(p.barcode,256,kind=k,index=i); bounded(p.note,MAX_TEXT,kind=k,index=i)
            listOfNotNull(p.packageMetric,p.servingMetric).forEach { positive(it.amount,k,i) }
            val units=listOfNotNull(p.reference.metricUnit,p.packageMetric?.unit,p.servingMetric?.unit)
            if (units.distinct().size > 1) fail(LibraryErrorCode.BASIS,k,i)
            if (p.basis==ProductFactBasis.METRIC_100 && (p.reference.metricAmount!=100.0 || p.reference.metricUnit==null)) fail(LibraryErrorCode.BASIS,k,i)
            if (p.basis==ProductFactBasis.REFERENCE_SERVING && p.servingMetric!=null &&
                (p.servingMetric.amount!=p.reference.metricAmount || p.servingMetric.unit!=p.reference.metricUnit)) fail(LibraryErrorCode.BASIS,k,i)
        }
        normalized.recipes.forEachIndexed { i,r ->
            currentCoroutineContext().ensureActive()
            val k=DefinitionKind.RECIPE
            if (r.yield!=null && r.yield<=0) fail(LibraryErrorCode.NUMBER,k,i)
            bounded(r.note,MAX_TEXT,kind=k,index=i)
            metadata(r.metadata,k,i)
            componentCount+=r.components.size.toLong()+r.ingredients.size
            if (r.components.size.toLong()+r.ingredients.size > MAX_ITEMS) fail(LibraryErrorCode.LIMIT,k,i)
            when(r.mode) {
                LibraryRecipeMode.REPORTED -> if (r.reportedPanel==null || r.ingredients.isNotEmpty()) fail(LibraryErrorCode.BASIS,k,i)
                LibraryRecipeMode.DERIVED -> if (r.reportedPanel!=null || r.components.isNotEmpty() || r.ingredients.isEmpty() || r.yield==null || r.metadata!=null) fail(LibraryErrorCode.BASIS,k,i)
            }
            r.reportedPanel?.let { panel(it,k,i) }
            if(metadataBytes(r.metadata)+metadataBytes(r.reportedPanel?.metadata)+r.components.sumOf { metadataBytes(it.metadata) }>65_536)fail(LibraryErrorCode.LIMIT,k,i)
            r.components.forEach { c ->
                bounded(c.name,512,true,k,i); portion(c.portion,k,i); c.nutrition?.let { facts(it,k,i) }; metadata(c.metadata,k,i)
                c.reference?.let { reference(it,keys,k,i) }
            }
            r.ingredients.forEach { ingredient ->
                reference(ingredient.reference,keys,k,i); measurement(ingredient.portion,k,i)
            }
        }
        normalized.meals.forEachIndexed { i,m ->
            currentCoroutineContext().ensureActive()
            val k=DefinitionKind.MEAL
            componentCount+=m.items.size
            if (m.items.isEmpty() || m.items.size>MAX_ITEMS) fail(LibraryErrorCode.LIMIT,k,i)
            metadata(m.metadata,k,i); m.reportedPanel?.let { panel(it,k,i) }
            if(metadataBytes(m.metadata)+metadataBytes(m.reportedPanel?.metadata)+m.items.sumOf { metadataBytes(it.metadata) }>65_536)fail(LibraryErrorCode.LIMIT,k,i)
            m.items.forEach { item ->
                bounded(item.name,512,true,k,i); facts(item.nutrition,k,i); portion(item.portion,k,i); metadata(item.metadata,k,i)
                item.reference?.let { reference(it,keys,k,i) }
                item.capturedPortion?.let { measurement(it,k,i) }
                item.servingCount?.let { positive(it,k,i) }; item.weightGrams?.let { positive(it,k,i) }
            }
        }
        if (componentCount>MAX_COMPONENTS) fail(LibraryErrorCode.LIMIT)
        val edges=normalized.recipes.associate { recipe -> recipe.externalId to
            (recipe.ingredients.map { it.reference }+recipe.components.mapNotNull { it.reference })
                .filter { it.kind==DefinitionKind.RECIPE }.map { it.externalId } }
        val depths=mutableMapOf<String,Int>()
        fun visit(id:String, path:Set<String>):Int {
            if (id in path) fail(LibraryErrorCode.CYCLE)
            if (path.size>=MAX_DEPTH) fail(LibraryErrorCode.LIMIT)
            depths[id]?.let { return it }
            val depth=1+(edges[id].orEmpty().maxOfOrNull { visit(it,path+id) } ?: 0)
            if(depth>MAX_DEPTH)fail(LibraryErrorCode.LIMIT)
            depths[id]=depth
            return depth
        }
        edges.keys.forEach { currentCoroutineContext().ensureActive(); visit(it,emptySet()) }
        val digests=linkedMapOf<LibraryKey,String>()
        suspend fun digest(key:LibraryKey, element:JsonElement) {
            currentCoroutineContext().ensureActive()
            val hash=Digest("SHA-256")
            hash+=canonical(element).toString().encodeToByteArray()
            digests[key]=hash.build().joinToString("") { (it.toInt() and 255).toString(16).padStart(2,'0') }
        }
        normalized.products.forEach { digest(LibraryKey(DefinitionKind.PRODUCT,it.externalId),json.encodeToJsonElement(it)) }
        normalized.recipes.forEach { digest(LibraryKey(DefinitionKind.RECIPE,it.externalId),json.encodeToJsonElement(it)) }
        normalized.meals.forEach { digest(LibraryKey(DefinitionKind.MEAL,it.externalId),json.encodeToJsonElement(it)) }
        return ValidatedLibrary(normalized,digests)
    }

    private fun facts(f:LibraryFacts,k:DefinitionKind,i:Int) {
        if (f.units!="kcal-g" || f.values.keys.any { it !in libraryNutrientKeys }) fail(LibraryErrorCode.UNIT,k,i)
        if (f.values.values.any { it!=null && (!it.isFinite() || it<0.0) }) fail(LibraryErrorCode.NUMBER,k,i)
        try { toValidatedEntityNutrients(f.toNutritionFacts()) } catch (_:IllegalArgumentException) { fail(LibraryErrorCode.NUMBER,k,i) }
    }
    private fun panel(p:LibraryPanel,k:DefinitionKind,i:Int) { facts(p.nutrition,k,i); portion(p.reference,k,i); metadata(p.metadata,k,i) }
    private fun portion(p:PortionDescription?,k:DefinitionKind,i:Int) {
        if(p==null)return
        p.amount?.let { positive(it,k,i) }; bounded(p.unitText,256,kind=k,index=i); bounded(p.text,MAX_TEXT,kind=k,index=i)
        if((p.metricAmount==null)!=(p.metricUnit==null)) fail(LibraryErrorCode.BASIS,k,i)
        p.metricAmount?.let { positive(it,k,i) }
    }
    private fun metadata(m:LibraryMetadata?,k:DefinitionKind,i:Int) {
        if(m==null)return
        bounded(m.notes,MAX_TEXT,kind=k,index=i); bounded(m.provenance,MAX_TEXT,kind=k,index=i)
        if(m.percentDailyValues.size>256 || m.observations.size>256 || json.encodeToString(m).encodeToByteArray().size>65_536) fail(LibraryErrorCode.LIMIT,k,i)
        m.percentDailyValues.forEach { (name,value) -> bounded(name,128,true,k,i); if(!value.isFinite() || value<0.0)fail(LibraryErrorCode.NUMBER,k,i) }
        m.observations.forEach { o -> bounded(o.kind,128,true,k,i); bounded(o.unitText,256,kind=k,index=i); bounded(o.text,MAX_TEXT,kind=k,index=i); if(o.value!=null && !o.value.isFinite())fail(LibraryErrorCode.NUMBER,k,i) }
    }
    private fun metadataBytes(m:LibraryMetadata?):Long=m?.let { json.encodeToString(it).encodeToByteArray().size.toLong() } ?: 0
    private fun reference(r:LibraryReference,keys:Set<LibraryKey>,k:DefinitionKind,i:Int) {
        if(r.kind==DefinitionKind.MEAL || LibraryKey(r.kind,r.externalId) !in keys) fail(LibraryErrorCode.REFERENCE,k,i)
    }
    private fun measurement(p:LibraryPortion,k:DefinitionKind,i:Int) {
        positive(p.amount,k,i)
        val measured=p.measurement()
        if(measured is com.maksimowiczm.foodyou.common.domain.measurement.Measurement.ImmutableMeasurement &&
            (!measured.metric.isFinite() || measured.metric<=0.0))fail(LibraryErrorCode.NUMBER,k,i)
    }
    private fun positive(value:Double,k:DefinitionKind,i:Int) { if(!value.isFinite() || value<=0.0)fail(LibraryErrorCode.NUMBER,k,i) }
    private fun bounded(value:String?,max:Int,required:Boolean=false,kind:DefinitionKind?=null,index:Int?=null) {
        if(required && value.isNullOrBlank())fail(LibraryErrorCode.IDENTITY,kind,index)
        if(value!=null && value.length>max)fail(LibraryErrorCode.LIMIT,kind,index)
    }
    private fun canonical(e:JsonElement):JsonElement = when(e) {
        is JsonObject -> {
            val values=e["values"] as? JsonObject
            val normalized=if(e["units"]==JsonPrimitive("kcal-g") && values!=null) {
                JsonObject(e+("values" to JsonObject(libraryNutrientKeys.associateWith { values[it] ?: JsonNull })))
            } else e
            JsonObject(normalized.entries.sortedBy { it.key }.associate { (key,value) -> key to canonical(value) })
        }
        is JsonArray -> JsonArray(e.map(::canonical))
        is JsonPrimitive -> if(!e.isString && e.doubleOrNull==0.0)JsonPrimitive(0.0) else e
    }
    private fun fail(code:LibraryErrorCode,kind:DefinitionKind?=null,index:Int?=null):Nothing = throw LibraryImportException(code,kind,index)

    companion object {
        const val MAX_BYTES=16*1024*1024
        const val MAX_RECORDS=20_000
        const val MAX_COMPONENTS=100_000
        const val MAX_ITEMS=10_000
        const val MAX_TEXT=16_384
        const val MAX_DEPTH=64
    }
}

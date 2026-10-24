# FOODUS-008 implementation plan

Owner: [Milestone B](../../../milestones/milestone-b.md#foodus-008), the sole Story
status owner. Coordinator owns builds, Samsung checks, independent review, trackers,
receipt and commit. Worker returns uncommitted source, regression and plan edits.

## Decision before implementation

The existing Android preview calls the same camera launch function as Take Photo.
Remove its discard confirmation and obsolete discard callback across expect/actual,
screen and ViewModel. Keep the current JPEG and scanner result unchanged while
acquisition is pending; only a successfully encoded replacement invokes the existing
onPhotoCaptured callback. Recheck CAMERA permission at each launch rather than
remembering a stale permission result. Null/canceled results, denied permission and
platform launch/decode/encoding failures leave the current usable photo untouched.
Keep the existing TakePicturePreview and gallery downscale mechanism and iOS
unsupported placeholder. Give the preview one accessible retake action.

## Required acceptance

Use the actual Android composable in a synthetic ComponentActivity host with safe
insets and the existing accessibility driver. Override LocalActivityResultRegistryOwner
with a controlled registry that accepts only the exact production camera, permission
and gallery contracts. Use generated bitmaps/JPEG and a synthetic URI; wrap LocalContext
to control permission answers without changing physical permissions. Assert immediate
single camera launch, retained image/state while pending and after cancel, denial,
launch/encoding errors, successful replacement/downscale, permission refresh and
gallery-origin retake. No real camera/picker, owner photos, diary/settings writes,
network, uninstall, clearing or device operations. Coordinator runs builds and Samsung
tests and records final acceptance; independent acquisition/state review is required.

## Evidence and continuation

Baseline HEAD: `63eab5af`. No installed harness modules. The Android preview now
launches existing capture directly and has one accessible retake description/action.
The obsolete discard-only expect/actual parameter, screen callback and ViewModel
method are removed; the iOS placeholder behavior is unchanged. Permission is queried
at every tap. Expected platform launch/decode/encoding exceptions retain the prior
state, while cancellation, fatal errors and unrelated exception types propagate;
the state callback remains outside encoding error handling. JPEG compression must
report success before a replacement is emitted.

`AiScanRetakeTest` exercises the actual production composable and ViewModel in the
existing DeveloperActivity ComponentActivity/ActivityScenario host. Its four methods:

- `previewLaunchesOnceRetainsReviewOnCancelAndReplacesOnlyOnSuccess`
- `permissionIsCheckedAtEachTapAndDenialOrRequestErrorRetainsReview`
- `cameraLaunchAndEncodingErrorsKeepReviewAndAllowRetry`
- `galleryOriginPreviewRetakesThroughCameraAndRetainsGalleryPhotoOnCancel`

The private registry requires exact TakePicturePreview, RequestPermission and
PickVisualMedia contracts and validates each input. Assertions cover identical
review/JPEG object retention and callback counts while pending and after failure,
single immediate camera launch, canceled result, denied/refreshed permission,
permission-launch/camera-launch errors, invalid recycled-bitmap encoding failure,
successful retry/reset, existing 1024px downscale, and generated gallery-file retake.
The gallery fixture deletes only its own uniquely created cache file. ViewModel
scope and accessibility flags are cleaned up; global Koin identity is preserved.
System camera/picker UI, permission UI and hardware are simulated to avoid private
content and global permission changes; this is not hardware camera acceptance.

Worker static whitespace check passed. No worker build or device action was run,
per dispatch ownership. Next: coordinator debug/unit/test-APK build, physical and
compact Samsung execution of the four methods, independent acquisition/state review,
then acceptance evidence and tracker/receipt/commit reconciliation. Story remains
In Progress until those gates pass.

## Coordinator final acceptance (2026-10-25)

Full unit/debug/test-APK Gradle command passed141 tests, zero failures/errors.
First phone invocation stopped all four cases at a fixture accessibility assertion:
Compose exposed Button role separately from the described actionable View. The
fixture now locates the visible exact preview description from one fresh host tree,
climbs only its ancestors to the enabled click target and invokes ACTION_CLICK.
All state/callback/contract assertions remain; no production change. Final fixture
APK rebuilt successfully with `:app:assembleDebugAndroidTest`.

Samsung SM-A057F Android15: all four cases passed physical1080x2400 in6.884s and
compact900x1600 in6.616s. Original display size/fixed policy/free rotation restored.
Actual production composable/ViewModel verifies same Result/JPEG while pending or
failed, exact single camera dispatch, denied/refreshed permission, launch and encoding
failure, successful retry/downscale and gallery-origin retention. Hardware, camera/
picker/permission UI remain controlled synthetic results, explicitly not tested as
real system UI/hardware. No owner data or physical permission changes occurred.

Inherited host limit accepted after review: DeveloperActivity's base lifecycle
observes the existing general secureScreen preference. No new private diary, AI
settings/credential or owner-photo reads, and no owner settings/data writes occur.
Earlier run shorthand saying no global settings reads is narrowed by this fact;
sealed prior receipts remain immutable. Fixtures' food, AI settings and media are
synthetic and the recorded evidence contains no sensitive values.

Independent `/root/review_foodus_008` PASS, including focused targeting correction.
Six-source aggregate `40836e763be42f4fa49e4dd86979ba1cc51bf84dad510a92c59bfc9df5c045e9`;
checked identity `4f28c0c624d81b4504b9f48fc220a0ee27e94e0c4044c86795c9b41177d7c4d9`.
Whitespace passed; no schema/dependency/version/release changes. Unsupported iOS
placeholder retained; native iOS runtime/build unavailable on this Windows host.
[Receipt](../../../_boss-runs/milestone-b-sequential/foodus-008-receipt.json)
prepared before terminal metadata; seal and authorized commit follow.

package com.maksimowiczm.foodyou.notification

import com.maksimowiczm.foodyou.common.infrastructure.koin.eventHandlerOf
import com.maksimowiczm.foodyou.notification.domain.AppNotificationRepository
import com.maksimowiczm.foodyou.notification.domain.RecordNotificationUseCase
import com.maksimowiczm.foodyou.notification.infrastructure.NotificationRecordingEventHandler
import com.maksimowiczm.foodyou.notification.infrastructure.RoomAppNotificationRepository
import com.maksimowiczm.foodyou.notification.infrastructure.room.NotificationDatabase
import org.koin.core.module.dsl.factoryOf
import org.koin.dsl.bind
import org.koin.dsl.module

val notificationModule = module {
    factory { get<NotificationDatabase>().notificationDao }
    factoryOf(::RoomAppNotificationRepository).bind<AppNotificationRepository>()
    factoryOf(::RecordNotificationUseCase)

    // Created at start so a notification published before the tab is ever opened is still recorded.
    eventHandlerOf(::NotificationRecordingEventHandler)
}

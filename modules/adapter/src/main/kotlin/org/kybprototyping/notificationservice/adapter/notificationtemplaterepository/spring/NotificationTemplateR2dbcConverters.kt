package org.kybprototyping.notificationservice.adapter.notificationtemplaterepository.spring

import org.springframework.core.convert.converter.Converter

object NotificationTemplateR2dbcConverter {

    internal object NotificationChannelEntityConverter : Converter<NotificationChannelEntity, NotificationChannelEntity>{
        override fun convert(source: NotificationChannelEntity): NotificationChannelEntity = source
    }

    internal object NotificationTypeEntityConverter : Converter<NotificationTypeEntity, NotificationTypeEntity>{
        override fun convert(source: NotificationTypeEntity): NotificationTypeEntity = source
    }

    internal object NotificationLanguageEntityConverter : Converter<NotificationLanguageEntity, NotificationLanguageEntity>{
        override fun convert(source: NotificationLanguageEntity): NotificationLanguageEntity = source
    }

}
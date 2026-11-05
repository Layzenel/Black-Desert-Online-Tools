package com.layzen.bdotools.core.account

/**
 * `UserCache\<id>\gamevariable.xml` как его прочитала платформа.
 *
 * @property modified время изменения файла в миллисекундах.
 * @property gameVariable текст файла или его часть с `CharacterOrderList`.
 */
data class AccountFile(val id: String, val modified: Long, val gameVariable: String)

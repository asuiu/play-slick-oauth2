package models.daos

import java.sql.Timestamp
import java.util.UUID
import com.google.inject.Inject
import models.entities.OauthAuthorizationCode
import models.persistence.SlickTables.OauthAuthorizationCodeTable
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.apache.commons.lang3.RandomUtils

trait OauthAuthorizationCodesDAO extends BaseDAO[OauthAuthorizationCodeTable, OauthAuthorizationCode] {
  def findByCode(code: String): Future[Option[OauthAuthorizationCode]]

  def delete(code: String): Future[Int]

  def createCode(accountId: Long, clientId: Long, redirectUri: Option[String]): Future[OauthAuthorizationCode]
}

class OauthAuthorizationCodesDAOImpl @Inject()(override protected val dbConfigProvider: DatabaseConfigProvider) extends OauthAuthorizationCodesDAO {

  import dbConfig.driver.api._


  override def findByCode(code: String): Future[Option[OauthAuthorizationCode]] = {
    val expireAt = new Timestamp(new DateTime().minusMinutes(30).getMillis)
    findByFilter(authCode => authCode.code === code && authCode.createdAt > expireAt).map(_.headOption)
  }

  override def delete(code: String): Future[Int] = deleteByFilter(_.code === code)

  override def createCode(accountId: Long, clientId: Long, redirectUri: Option[String]): Future[OauthAuthorizationCode] = {
    val createdAt = new Timestamp(new DateTime().getMillis)
    val row = OauthAuthorizationCode(
      id = 0,
      accountId,
      clientId,
      UUID.randomUUID.toString,
      redirectUri: Option[String],
      createdAt)
    insert(row).map(_ => row)
  }
}

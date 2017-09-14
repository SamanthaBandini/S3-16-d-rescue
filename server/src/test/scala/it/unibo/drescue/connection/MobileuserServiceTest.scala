package it.unibo.drescue.connection

import it.unibo.drescue.communication.GsonUtils
import it.unibo.drescue.communication.builder.requests.{ChangePasswordMessageBuilderImpl, SignUpMessageBuilderImpl}
import it.unibo.drescue.communication.messages.requests.{LoginMessageImpl, RequestProfileMessageImpl}
import it.unibo.drescue.communication.messages.{Message, MessageType}
import it.unibo.drescue.database.dao.UserDao
import it.unibo.drescue.database.exceptions.DBQueryException
import it.unibo.drescue.database.{DBConnection, DBConnectionImpl}
import it.unibo.drescue.model.{User, UserImplBuilder}
import org.scalatest.FunSuite

class MobileuserServiceTest extends FunSuite {

  private val correctEmail: String = "j.doe@test.com"
  private val correctPassword: String = "test"
  private val incorrectEmail: String = "john.doe@test.com"
  private val incorrectPassword: String = "admin"
  private val userName: String = "John"
  private val userPhoneNumber: String = "3333333333"
  private val userSurname: String = "Doe"
  private val userTest: User = new UserImplBuilder()
    .setName(userName)
    .setSurname(userSurname)
    .setEmail(correctEmail)
    .setPassword(correctPassword)
    .setPhoneNumber(userPhoneNumber)
    .createUserImpl()


  var dBConnection: DBConnectionImpl = _

  def startCommunicationAndInsertUser(): UserDao = {
    startCommunication()
    insertUserIntoDb()
  }

  def startCommunication(): Unit = {
    dBConnection = DBConnectionImpl.getLocalConnection
  }

  def insertUserIntoDb(): UserDao = {
    val userDao = (dBConnection getDAO DBConnection.Table.USER).asInstanceOf[UserDao]
    userDao insert userTest
    userDao
  }

  def endSignUpCommunication(): Unit = {
    val userDao = (dBConnection getDAO DBConnection.Table.USER).asInstanceOf[UserDao]
    endCommunication(userDao)
  }

  def endCommunication(userDao: UserDao): Unit = {
    userDao delete userTest
    dBConnection closeConnection()
  }

  def signUpCommunication(): Option[Message] = {
    val messageSignUp: Message = new SignUpMessageBuilderImpl()
      .setName(userName)
      .setSurname(userSurname)
      .setEmail(correctEmail)
      .setPassword(correctPassword)
      .setPhoneNumber(userPhoneNumber)
      .build()
    mobileuserCommunication(messageSignUp)
  }

  def mobileuserCommunication(mobileuserMessage: Message): Option[Message] = {
    val message: String = GsonUtils toGson mobileuserMessage
    val service: ServiceOperation = new MobileuserService
    service.accessDB(dBConnection, message)
  }

  def loginCommunication(email: String, password: String): Option[Message] = {
    val messageLogin: Message = new LoginMessageImpl(email, password)
    mobileuserCommunication(messageLogin)
  }

  def changePasswordCommunication(oldPassword: String, newPassword: String): Option[Message] = {
    val changePasswordMessage: Message = new ChangePasswordMessageBuilderImpl()
      .setUserEmail(correctEmail)
      .setOldPassword(oldPassword)
      .setNewPassword(newPassword)
      .build()
    mobileuserCommunication(changePasswordMessage)
  }

  test("User should do correct sign up") {
    startCommunication()
    val responseMessage: Option[Message] = signUpCommunication()
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.SUCCESSFUL_MESSAGE.getMessageType)
      case None => fail()
    }
    endSignUpCommunication()
  }

  test("User should not do correct sign up for duplicate email") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = signUpCommunication()
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.ERROR_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User with email '" + correctEmail + "' and password '"
    + correctPassword + "' should enter correct data for login") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = loginCommunication(correctEmail, correctPassword)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.RESPONSE_LOGIN_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User with email '" + correctEmail + "' and password '"
    + incorrectPassword + "' should not enter correct password for login") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = loginCommunication(correctEmail, incorrectPassword)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.ERROR_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User with email '" + incorrectEmail + "' and password '"
    + correctPassword + "' should not enter correct email for login") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = loginCommunication(incorrectEmail, correctPassword)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.ERROR_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User should be able to change password") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = changePasswordCommunication(correctPassword, incorrectPassword)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.SUCCESSFUL_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User should not be able to change password for incorrect old password") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = changePasswordCommunication(incorrectPassword, correctPassword)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.ERROR_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User should not be able to change password for incorrect new password") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val responseMessage: Option[Message] = changePasswordCommunication(correctPassword, correctPassword)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.ERROR_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("User should get his profile") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val profileMessage: Message = new RequestProfileMessageImpl(correctEmail)
    val responseMessage: Option[Message] = mobileuserCommunication(profileMessage)
    responseMessage match {
      case Some(responseMessage) =>
        assert(responseMessage.getMessageType == MessageType.PROFILE_MESSAGE.getMessageType)
      case None => fail()
    }
    endCommunication(userDao)
  }

  test("Profile request with wrong email should produce DBQueryException") {
    val userDao: UserDao = startCommunicationAndInsertUser()
    val profileMessage: Message = new RequestProfileMessageImpl(incorrectEmail)
    assertThrows[DBQueryException] {
      mobileuserCommunication(profileMessage)
    }
    endCommunication(userDao)
  }

}

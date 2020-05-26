package lectures.part4implicits

object TypeClasses extends App {

  trait HTMLWritable {
    def toHTML: String
  }

  case class User(name: String, age: Int, email: String) extends HTMLWritable {
    def toHTML: String = s"<div>$name ($age) <a href=$email>Email</a></div>"
  }

  User("John", 23, "john@test.com").toHTML

  /**
    * 1 - works for custom types only
    * 2 - only one implementation
    */
  // option 2 - use pattern matching
  // object HTMLSerializerPM {
  //   def serializeToHTML(value: any) = value match {
  //     case User(n, a, e)  =>
  //     case java.util.Date =>
  //     case _              =>
  //   }
  // }

  /**
    * 1 - no type safety
    * 2 - need to modify code
    * 3 - only one implementation
    */
  // option 3 - type class
  trait HTMLSerializer[T] {
    def serialize(value: T): String
  }

  object UserSerializer extends HTMLSerializer[User] {
    def serialize(user: User): String =
      s"<div>${user.name} (${user.age}) <a href=${user.email}>Email</a></div>"
  }

  val john = User("John", 23, "john@test.com")
  println(UserSerializer.serialize(john))

  /**
    * 1 - can implement serializers for other types
    * 2 - can define multiple serializers for one type
    */
  trait TypeClassTemplate[T] {
    def action(value: T): String
  }
}

package packaged.macros

import scala.gestalt._

class InnerClassMacro {
  def createInner = new Inner()
  class Inner {
    def plus(a: Int, b: Int) = meta {
      import toolbox._
      q"$a + $b"
    }
  }
  object InnerObject {
    def plus(a: Int, b: Int) = meta {
      import toolbox._
      q"$a + $b"
    }
  }
}

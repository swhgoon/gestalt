package scala.gestalt
package parsing

import Parsers._

trait TreeHelper { self: Parser =>
  import self.tb._

  private implicit class TreeOps(val tree: Tree) {
    def select(name: String): Tree = Select(tree, name)

    def appliedTo(args: Tree*): t.Tree = Apply(tree, args.toList)

    def appliedToType(args: TypeTree*): Tree = ApplyType(tree, args.toList)
  }

  // lifted trees
  lazy val scalaNil = select("scala.Nil")
  lazy val scalaList = select("scala.List")
  lazy val scalaSome = select("scala.Some")
  lazy val scalaNone = select("scala.None")
  lazy val toolbox = Ident(tbName)
  lazy val root = Ident("_root_")

  private def select(path: String, isTerm: Boolean = true): Tree = {
    val parts = path.split('.')

    val qual = parts.init.foldLeft[t.Tree](root) { (prefix, name) =>
      prefix.select(name)
    }

    if (isTerm) t.Select(qual, parts.last) else t.TypeSelect(qual, parts.last)
  }

  /* -------------------- lifting terms -------------------------- */
  def liftLit(value: Any): Tree = toolbox.select("Lit").appliedTo(Lit(null))

  def liftIdent(name: String): Tree = toolbox.select("Ident").appliedTo(Lit(name))

  def liftSelect(qual: Tree, name: String): Tree = toolbox.select("Select").appliedTo(qual, Lit(name))

  def liftThis(qual: String): Tree = toolbox.select("This").appliedTo(Lit(qual))

  def liftSuper(thisp: String, superp: String): Tree =
    toolbox.select("This").appliedTo(Lit(thisp), Lit(superp))

  def liftInfix(lhs: Tree, op: String, rhs: Tree): Tree =
    toolbox.select("Infix").appliedTo(lhs, liftIdent(op), rhs)

  def liftPostfix(od: Tree, op: String): Tree =
    toolbox.select("Postfix").appliedTo(od, liftIdent(op))

  def liftBlock(trees: Seq[Tree]): Tree = {
    val args = trees.foldLeft(scalaNil) { (acc, tree) => tb.Infix(tree, "::", acc) }
    toolbox.select("Block").appliedTo(args)
  }

  def liftTuple(trees: Seq[Tree]): Tree = {
    val args = trees.foldLeft(scalaNil) { (acc, tree) => tb.Infix(tree, "::", acc) }
    toolbox.select("Tuple").appliedTo(args)
  }

  def liftInterpolate(tag: String, parts: Seq[String], args: Seq[Tree]): Tree = {
    val liftedParts = parts.foldLeft(scalaNil) { (acc, str) => tb.Infix(str, "::", acc) }
    val liftedArgs = args.foldLeft(scalaNil) { (acc, tree) => tb.Infix(tree, "::", acc) }
    toolbox.select("Interpolate").appliedTo(Lit(tag), liftedParts, liftedArgs)
  }

  /* -------------------- lifting types -------------------------- */
  def liftTypeIdent(name: String): Tree = toolbox.select("TypeIdent").appliedTo(Lit(name))

  def liftTypeSelect(qual: Tree, name: String): Tree =
    toolbox.select("TypeSelect").appliedTo(qual, Lit(name))

  def liftTypeApplyInfix(lhs: Tree, op: String, rhs: Tree): Tree =
    toolbox.select("TypeApplyInfix").appliedTo(lhs, liftIdent(op), rhs)

  def liftTypeFunction(params: Seq[Tree], res: Tree): Tree = {
    val liftedParams = args.foldLeft(scalaNil) { (acc, tree) => Infix(tree, "::", acc) }
    toolbox.select("TypeFunction").appliedTo(liftedParams, res)
  }

  def liftTypeRefine(tpe: Tree, stats: Seq[Tree]) = {
    val liftedTpe = if (tpe == null) scalaNone else scalaSome.appliedTo(tpe)
    val liftedStats = stats.foldLeft(scalaNil) { (acc, tree) => Infix(tree, "::", acc) }
    toolbox.select("TypeRefine").appliedTo(liftedTpe, liftedStats)
  }

  def liftTypeAnd(lhs: Tree, rhs: Tree): Tree =
    toolbox.select("TypeAnd").appliedTo(lhs, rhs)

  def liftTypeOr(lhs: Tree, rhs: Tree): Tree =
    toolbox.select("TypeOr").appliedTo(lhs, rhs)

  def liftTypeAnnotated(tpe: Tree, annots: Seq[Tree]): Tree = {
    val liftedAnnots = annots.foldLeft(scalaNil) { (acc, tree) => Infix(tree, "::", acc) }
    toolbox.select("TypeAnnotated").appliedTo(tpe, liftedAnnots)
  }

  def liftTypeSingleton(ref: Tree): Tree =
    toolbox.select("TypeSingleton").appliedTo(ref)

  def liftTypeApply(tpe: Tree, args: Seq[Tree]): Tree = {
    val liftedArgs = args.foldLeft(scalaNil) { (acc, tree) => Infix(tree, "::", acc) }
    toolbox.select("TypeApply").appliedTo(tpe, liftedArgs)
  }

  def liftyTypeByName(tpe: Tree): Tree = toolbox.select("TypeByName").appliedTo(tpe)

  def liftyTypeRepeated(tpe: Tree): Tree = toolbox.select("TypeRepeated").appliedTo(tpe)

  def liftTypeBounds(lo: Tree, hi: Tree): Tree = {
    val liftedLO = if (lo == null) scalaNone else scalaSome.appliedTo(lo)
    val liftedHI = if (hi == null) scalaNone else scalaSome.appliedTo(hi)
    toolbox.select("TypeBounds").appliedTo(liftedLO, liftedHI)
  }
}


package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.grunnlag

interface Grunnlag {
    fun accept(grunnlagsVisitor: GrunnlagsVisitor)

    fun dataObject(): Any
}

interface GrunnlagsVisitor {
    fun visit(grunnlag: Grunnlag)
}

class GrunnlagVisitor private constructor() : GrunnlagsVisitor {

    private val grunnlagObjects = mutableListOf<Grunnlag>()

    override fun visit(grunnlag: Grunnlag) {
        grunnlagObjects.add(grunnlag)
    }

    private fun getAllGrunnlag(): List<Grunnlag> = grunnlagObjects.toList()

    companion object {
        fun listAllGrunnlag(grunnlag: Grunnlag): List<Grunnlag> =
            GrunnlagVisitor().also { it.visit(grunnlag) }.getAllGrunnlag()

        inline fun <reified Type> listGrunnlagOfType(grunnlag: Grunnlag): List<Grunnlag> =
            listAllGrunnlag(grunnlag).filter { it is Type }

        inline fun <reified Type> listDataObjectOfType(grunnlag: Grunnlag): List<Any> =
            listAllGrunnlag(grunnlag).map { it.dataObject() }.filter { it is Type }
    }
}
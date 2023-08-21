package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.utils

import no.nav.pensjon.opptjening.omsorgsopptjening.felles.mapper

inline fun <reified T> List<T>.serialize(): String {
    val listType = mapper.typeFactory.constructCollectionLikeType(List::class.java, T::class.java)
    return mapper.writerFor(listType).writeValueAsString(this)
}

inline fun <reified T> String.deserializeList(): List<T> {
    val listType = mapper.typeFactory.constructCollectionLikeType(List::class.java, T::class.java)
    return mapper.readerFor(listType).readValue(this)
}
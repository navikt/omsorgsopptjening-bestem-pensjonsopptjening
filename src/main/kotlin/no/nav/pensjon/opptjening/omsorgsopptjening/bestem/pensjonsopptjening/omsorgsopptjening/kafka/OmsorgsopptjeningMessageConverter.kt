package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.kafka//package no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.kafka.producer
//
//import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.common.mapToJson
//import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.DomainKilde
//import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsarbeid.model.Landstilknytning
//import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.omsorgsopptjening.OmsorgsOpptjening
//import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.person.model.Person
//import no.nav.pensjon.opptjening.omsorgsopptjening.bestem.pensjonsopptjening.vilkarsvurdering.vilkar.Utfall
//import no.nav.pensjon.opptjening.omsorgsopptjening.felles.domene.kafka.messages.OmsorgsOpptjeningKey
//
//
//
//fun OmsorgsOpptjening.createKafkaKey(): String = OmsorgsOpptjeningKey(
//    omsorgsAr = omsorgsAr,
//    omsorgsyter = omsorgsyter.fnr,
//    utfall = mapToKafkaUtfall(utfall)
//).mapToJson()
//
//fun OmsorgsOpptjening.createKafkaValue(): String = KafkaOmsorgsOpptjening(
//    omsorgsAr = omsorgsAr,
//    omsorgsyter = KafkaPerson(omsorgsyter.gjeldendeFnr.fnr),
//    omsorgsmottakereInvilget = omsorgsmottakereInvilget.map { KafkaPerson(it.gjeldendeFnr.fnr) },
//    grunnlag = map(omsorgsGrunnlag),
//    vilkarsResultat = vilkarsResultat.mapToJson(),
//    utfall = mapToKafkaUtfall(utfall)
//).mapToJson()
//
//private fun mapToKafkaUtfall(utfall: Utfall): KafkaUtfall =
//    when (utfall) {
//        Utfall.INVILGET -> KafkaUtfall.INVILGET
//        Utfall.AVSLAG -> KafkaUtfall.AVSLAG
//        Utfall.SAKSBEHANDLING -> KafkaUtfall.SAKSBEHANDLING
//        Utfall.MANGLER_ANNEN_OMSORGSYTER -> KafkaUtfall.MANGLER_ANNEN_OMSORGSYTER
//    }
//
//fun map(omsorgsGrunnlag: KafkaOmsorgsarbeidsSnapshot): KafkaOmsorgsarbeidsSnapshot {
//    return KafkaOmsorgsarbeidsSnapshot(
//        omsorgsyter = convertPerson(omsorgsGrunnlag.omsorgsyter),
//        omsorgsAr = omsorgsGrunnlag.omsorgsAr,
//        omsorgstype = convertToOmsorgsarbeidsType(omsorgsGrunnlag.omsorgstype),
//        kjoreHash = omsorgsGrunnlag.kjoreHashe,
//        kilde = convertToOmsorgsarbeidsKilde(omsorgsGrunnlag.kilde),
//        omsorgsSaker = omsorgsGrunnlag.omsorgsSaker.map { sak ->
//            KafkaOmsorgsgrunnlagMelding.Sak(
//                omsorgsyter = convertPerson(omsorgsGrunnlag.omsorgsyter),
//                omsorgVedtakPeriode = sak.omsorgVedtakPeriode.map { periode ->
//                    KafkaOmsorgsgrunnlagMelding.VedtakPeriode(
//                        fom = periode.fom,
//                        tom = periode.tom,
//                        prosent = periode.prosent,
//                        omsorgsmottaker = convertPerson(periode.omsorgsmottaker)
//                    )
//                }
//            )
//        }
//    )
//}
//
//private fun convertPerson(person: Person) =
//    KafkaPerson(
//        fnr = person.gjeldendeFnr.fnr,
//        fodselsAr = person.fodselsAr.toString()
//    )
//
//private fun convertToOmsorgsarbeidsType(omsorgsType: KafkaOmsorgstype) = when (omsorgsType) {
//    KafkaOmsorgstype.BARNETRYGD -> KafkaOmsorgstype.BARNETRYGD
//    KafkaOmsorgstype.HJELPESTONAD_SATS_3 -> KafkaOmsorgstype.HJELPESTØNAD_SATS_3
//    KafkaOmsorgstype.HJELPESTONAD_SATS_4 -> KafkaOmsorgstype.HJELPESTØNAD_SATS_4
//}
//
//private fun convertToOmsorgsarbeidsKilde(kilde: DomainKilde) = when (kilde) {
//    DomainKilde.BARNETRYGD -> KafkaKilde.BARNETRYGD
//    DomainKilde.INFOTRYGD -> KafkaKilde.INFOTRYGD
//}
//
//private fun convertLandstilknytning(tilknytning: Landstilknytning) = when (tilknytning) {
//    Landstilknytning.EOS -> KafkaLandstilknytning.EØS
//    Landstilknytning.NASJONAL -> KafkaLandstilknytning.NASJONAL
//}
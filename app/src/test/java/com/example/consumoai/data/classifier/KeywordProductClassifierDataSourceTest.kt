package com.example.consumoai.data.classifier

import com.example.consumoai.domain.model.ProductCategory
import com.example.consumoai.domain.model.ProductItem
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Tests for KeywordProductClassifierDataSource (V1.1 - 2026-05-14)
 *
 * Objective: Validate that ~125+ items from OTHER are reclassified to proper categories
 * based on expanded keywords and special rule functions.
 *
 * Status: Covers beverages, hygiene, cleaning, industrialized, basic_food, and produce
 */
class KeywordProductClassifierDataSourceTest {

    private val classifier = KeywordProductClassifierDataSource()

    // ========== BEVERAGES (including alcoholic and energy drinks) ==========

    @Test
    fun classify_beverage_softDrinks() {
        assertCategory("COCA-COLA ORIGINAL LATA 350ML", ProductCategory.BEVERAGES)
        assertCategory("SUCO NATURALE LARANJA", ProductCategory.BEVERAGES)
        assertCategory("GUARANA 2L", ProductCategory.BEVERAGES)
        assertCategory("FANTA UVAICE 1LT", ProductCategory.BEVERAGES)
        assertCategory("SPRITE 2LT", ProductCategory.BEVERAGES)
    }

    @Test
    fun classify_beverage_alcoholic() {
        // Beer - common brands and styles
        assertCategory("CHOPP TUPINIQUIM IPA 1L", ProductCategory.BEVERAGES)
        assertCategory("CV BADEN BADEN IPA LT 473ML", ProductCategory.BEVERAGES)
        assertCategory("CV BLUE MOON BELGIAN LT 350ML", ProductCategory.BEVERAGES)
        assertCategory("KAISERDOM PREMIUM 350ML", ProductCategory.BEVERAGES)
        assertCategory("PILSEN BRAHMA 350ML", ProductCategory.BEVERAGES)

        // Wine
        assertCategory("VH AURORA C.SAUV 750ML", ProductCategory.BEVERAGES)
        assertCategory("VINHO CONCHA Y TORO 750ML", ProductCategory.BEVERAGES)
    }

    @Test
    fun classify_beverage_energy() {
        assertCategory("ENERG MONSTER 473ML", ProductCategory.BEVERAGES)
        assertCategory("ENERGETICO RED BULL 250ML", ProductCategory.BEVERAGES)
    }

    @Test
    fun classify_beverage_water() {
        assertCategory("AGUA MINERAL PUREZA 1.5L", ProductCategory.BEVERAGES)
        assertCategory("CHA GELADO", ProductCategory.BEVERAGES)
    }

    // ========== HYGIENE ==========

    @Test
    fun classify_hygiene_paperProducts() {
        assertCategory("P H NEVE T.SEDA DUPL L16P15 30M", ProductCategory.HYGIENE)
        assertCategory("PAPEL HIGIENICO SOFT 30M", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_hairCare() {
        assertCategory("SH HEAD&SHOULDERS A.COC 650ML", ProductCategory.HYGIENE)
        assertCategory("SH CLEAR MEN LIMP PROF 400ML", ProductCategory.HYGIENE)
        assertCategory("CONDICIONADOR PANTENE 200ML", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_personalCare() {
        assertCategory("SABONETE DOVE 90G", ProductCategory.HYGIENE)
        assertCategory("DESODORANTE REXONA CLINICAL MEN 150ML", ProductCategory.HYGIENE)
        assertCategory("CARGA GILLETTE MACH3 SENS C/2", ProductCategory.HYGIENE)
        assertCategory("CR D COLGATE T.ACAO 180G PROM", ProductCategory.HYGIENE)
        assertCategory("ESCOVA DENTAL COLGATE", ProductCategory.HYGIENE)
        assertCategory("FIO DENTAL ORAL-B 50M", ProductCategory.HYGIENE)
        assertCategory("ABSORVENTE KOTEX PROTEPLUS", ProductCategory.HYGIENE)
        assertCategory("FRALDA PAMPERS RN", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_wipes() {
        assertCategory("TOALHA UMED HUGGIES L4P3 C/48UN", ProductCategory.HYGIENE)
        assertCategory("LENCO UMED HUGGIES T.PROT C/88", ProductCategory.HYGIENE)
        assertCategory("LENCO UMEDECIDO KLEENEX", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_hygiene_otherProducts() {
        assertCategory("ALGODAO JOHNSON 100G", ProductCategory.HYGIENE)
        assertCategory("COTONETE JOHNSON", ProductCategory.HYGIENE)
        assertCategory("PRESERVATIVO OLLA", ProductCategory.HYGIENE)
    }

    // ========== CLEANING ==========

    @Test
    fun classify_cleaning_dishDetergent() {
        assertCategory("DET LQ LOUCA LIMPOL LIMAO 500ML", ProductCategory.CLEANING)
        assertCategory("DETERGENTE SUNDOWN 500ML", ProductCategory.CLEANING)
        assertCategory("LAV LOUCA ULTRAPURO 500ML", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_laundry() {
        assertCategory("L ROUP PO OMO L.PERFEITA 2,2KG", ProductCategory.CLEANING)
        assertCategory("LAVA ROUPAS PO ARIEL 1.5KG", ProductCategory.CLEANING)
        assertCategory("AMAC CONC COMFORT FRESCOR 1L", ProductCategory.CLEANING)
        assertCategory("AMACIANTE DOWNY 900ML", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_floors() {
        assertCategory("LIMP AJAX FRESH PODER 1L", ProductCategory.CLEANING)
        assertCategory("LIMP PISO MAD DESTAC L&VAN 750ML", ProductCategory.CLEANING)
        assertCategory("MULTIUSO SPRAY AJAX", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_disinfectants() {
        assertCategory("DESINF PINHO SOL ORIG L500P450ML", ProductCategory.CLEANING)
        assertCategory("DESINFETANTE LYSOFORM", ProductCategory.CLEANING)
        assertCategory("AGUA SANITARIA 1L", ProductCategory.CLEANING)
    }

    @Test
    fun classify_cleaning_otherProducts() {
        assertCategory("SACO LIXO UTILO 50L C/10", ProductCategory.CLEANING)
        assertCategory("ESPONJA ESFREBOM BOMBRIL", ProductCategory.CLEANING)
        assertCategory("P TOALHA MILI 3X200F", ProductCategory.CLEANING)
        assertCategory("PAPEL TOALHA ROLL 200M", ProductCategory.CLEANING)
        assertCategory("FILTRO PAPEL LITE", ProductCategory.CLEANING)
    }

    // ========== INDUSTRIALIZED ==========

    @Test
    fun classify_industrialized_pizza() {
        assertCategory("PIZZA SADIA MUSSARELA 440G", ProductCategory.INDUSTRIALIZED)
        assertCategory("PIZZA SEARA CALABRESA 500G", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_pasta() {
        assertCategory("LASANHA SADIA BOLONHESA MN350G", ProductCategory.INDUSTRIALIZED)
        assertCategory("MAC NISSIN GALINHA CAIPIRA 85G", ProductCategory.INDUSTRIALIZED)
        assertCategory("NISSIN LAMEN FRANGO 85G", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_snacks() {
        assertCategory("SALG FANDANGOS PRESUNTO 160G", ProductCategory.INDUSTRIALIZED)
        assertCategory("SALG DORITOS MOSTARDA HEINZ 110G", ProductCategory.INDUSTRIALIZED)
        assertCategory("SALG CHEETOS ONDA REQUEIJAO 105G", ProductCategory.INDUSTRIALIZED)
        assertCategory("SALGADINHO FRITO LAY", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_sweets() {
        assertCategory("TRENTO DARK 55% CACAU 29G", ProductCategory.INDUSTRIALIZED)
        assertCategory("BOLO MARMORE S.BOYS 250G", ProductCategory.INDUSTRIALIZED)
        assertCategory("ROSCA POLV TRAD GUSMAN 70G", ProductCategory.INDUSTRIALIZED)
        assertCategory("CHOCOLATE BRIGADEIRO", ProductCategory.INDUSTRIALIZED)
        assertCategory("BALA HALLS MENTA", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_snackFood() {
        assertCategory("PIPOCA MIC YOKI COB CARAM 160G", ProductCategory.INDUSTRIALIZED)
        assertCategory("COOKIE BAUDUCCO CHOCOLATE", ProductCategory.INDUSTRIALIZED)
        assertCategory("WAFER BAUDUCCO CHOCOLATE", ProductCategory.INDUSTRIALIZED)
        assertCategory("BISCOITO ISABELA", ProductCategory.INDUSTRIALIZED)
        assertCategory("WRAP TORTILHA FRESCATA", ProductCategory.INDUSTRIALIZED)
    }

    @Test
    fun classify_industrialized_sauces() {
        assertCategory("MAIONESE HELLMANN 500ML", ProductCategory.INDUSTRIALIZED)
        assertCategory("KETCHUP HEINZ 1KG", ProductCategory.INDUSTRIALIZED)
        assertCategory("MOSTARDA HEINZ 390G", ProductCategory.INDUSTRIALIZED)
        assertCategory("TEMPERO SAZON 60G", ProductCategory.INDUSTRIALIZED)
    }

    // ========== BASIC FOOD ==========

    @Test
    fun classify_basicFood_meat() {
        assertCategory("PATINHO ZAFFARI NOV E.LIMPO", ProductCategory.BASIC_FOOD)
        assertCategory("COXAO DENTRO PEDACO", ProductCategory.BASIC_FOOD)
        assertCategory("CARNE MOIDA 500G", ProductCategory.BASIC_FOOD)
        assertCategory("FRANGO INTEIRO SADIA", ProductCategory.BASIC_FOOD)
        assertCategory("PEITO FRANGO CONGELADO", ProductCategory.BASIC_FOOD)
        assertCategory("SALSICHA SADIA 500G", ProductCategory.BASIC_FOOD)
        assertCategory("LINGUICA PERDIGAO 500G", ProductCategory.BASIC_FOOD)
        assertCategory("PRESUNTO SADIA FATIADO", ProductCategory.BASIC_FOOD)
        assertCategory("MORTADELA SADIA 500G", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_dairy() {
        assertCategory("LEITE INTEGRAL PARMALAT 1L", ProductCategory.BASIC_FOOD)
        assertCategory("IOGURTE ATIVA 540G", ProductCategory.BASIC_FOOD)
        assertCategory("IOG NATURAL INTEGRAL", ProductCategory.BASIC_FOOD)
        assertCategory("QJO MUSSARELA PRES FAT 300G", ProductCategory.BASIC_FOOD)
        assertCategory("QUEIJO MOZZARELLA SADIA", ProductCategory.BASIC_FOOD)
        assertCategory("REQUEIJAO VIGOR CREM TRAD 400G", ProductCategory.BASIC_FOOD)
        assertCategory("MANTEIGA COM SAL BRIT", ProductCategory.BASIC_FOOD)
        assertCategory("MARGARINA DELICIA", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_tomatoProducts() {
        assertCategory("MOLHO TOMATE HEINZ 350G", ProductCategory.BASIC_FOOD)
        assertCategory("PASSATA MOLISANA 690G", ProductCategory.BASIC_FOOD)
        assertCategory("EXTRATO TOMATE CASARÃO", ProductCategory.BASIC_FOOD)
        assertCategory("TOMATE PELADO MUTTI 400G", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_grains() {
        assertCategory("ARROZ TIPO 1 INTEGRAL", ProductCategory.BASIC_FOOD)
        assertCategory("FEIJAO PRETO 1KG", ProductCategory.BASIC_FOOD)
        assertCategory("MACARRAO SEMOLINA GALLO", ProductCategory.BASIC_FOOD)
        assertCategory("ESPAGUETE GALLO 500G", ProductCategory.BASIC_FOOD)
        assertCategory("FARINHA BRANCA DONA BENTA", ProductCategory.BASIC_FOOD)
    }

    @Test
    fun classify_basicFood_otherProducts() {
        assertCategory("CAFE PREMIUM 500G", ProductCategory.BASIC_FOOD)
        assertCategory("ACUCAR CRISTAL CRISTALINO 1KG", ProductCategory.BASIC_FOOD)
        assertCategory("SAL REFINADO EXTRA IOD CISNE 1KG", ProductCategory.BASIC_FOOD)
        assertCategory("OLEO SOJA CARGILL 900ML", ProductCategory.BASIC_FOOD)
        assertCategory("MEL APIARIO PADRE ASSIS 500G", ProductCategory.BASIC_FOOD)
        assertCategory("AVEIA FLOCOS FINOS TUTTI 170G", ProductCategory.BASIC_FOOD)
        assertCategory("AMENDOIM BCO PREM F.FRIDA 400G", ProductCategory.BASIC_FOOD)
        assertCategory("MILHO VERDE 700G", ProductCategory.BASIC_FOOD)
        assertCategory("PAO FRANCES DIARIO", ProductCategory.BASIC_FOOD)
        assertCategory("CACETINHO PRADO", ProductCategory.BASIC_FOOD)
        assertCategory("OVO CAIPIRA DZIA 30UN", ProductCategory.BASIC_FOOD)
        assertCategory("ATUM GOMES DA COSTA 170G", ProductCategory.BASIC_FOOD)
        assertCategory("SARDINHA CONSERVA", ProductCategory.BASIC_FOOD)
    }

    // ========== PRODUCE ==========

    @Test
    fun classify_produce_fruits() {
        assertCategory("BANANA PRATA", ProductCategory.PRODUCE)
        assertCategory("MACA RED 1KG", ProductCategory.PRODUCE)
        assertCategory("MAMAO PAPAYA", ProductCategory.PRODUCE)
        assertCategory("LARANJA PERA MUDA", ProductCategory.PRODUCE)
        assertCategory("MORANGO 500G", ProductCategory.PRODUCE)
        assertCategory("UVA VERDE 1KG", ProductCategory.PRODUCE)
        assertCategory("LIMAO TAHITI", ProductCategory.PRODUCE)
        assertCategory("ABACAXI PEROLA", ProductCategory.PRODUCE)
        assertCategory("MELANCIA ESTACAO CUBO AS", ProductCategory.PRODUCE)
        assertCategory("MELAO 1,5KG", ProductCategory.PRODUCE)
        assertCategory("PERA IMPORTADA", ProductCategory.PRODUCE)
    }

    @Test
    fun classify_produce_vegetables() {
        assertCategory("TOMATE ITALIANO 500G", ProductCategory.PRODUCE)
        assertCategory("BATATA COMUM 1KG", ProductCategory.PRODUCE)
        assertCategory("CENOURA 1KG", ProductCategory.PRODUCE)
        assertCategory("CEBOLA ROXA 1KG", ProductCategory.PRODUCE)
        assertCategory("ALFACE HIDROPONICA", ProductCategory.PRODUCE)
        assertCategory("PIMENTAO VERDE GRANEL", ProductCategory.PRODUCE)
        assertCategory("CEBOLINHA FRESCA 100G", ProductCategory.PRODUCE)
        assertCategory("ALHO GRANEL", ProductCategory.PRODUCE)
    }

    @Test
    fun classify_produce_greenVegetables() {
        assertCategory("BROCOLIS 500G", ProductCategory.PRODUCE)
        assertCategory("COUVE MINEIRA FRESCA", ProductCategory.PRODUCE)
        assertCategory("REPOLHO VERDE 1KG", ProductCategory.PRODUCE)
        assertCategory("PEPINO COMUM", ProductCategory.PRODUCE)
        assertCategory("ABOBRINHA ITALIANA", ProductCategory.PRODUCE)
        assertCategory("BERINJELA ROXA", ProductCategory.PRODUCE)
    }

    @Test
    fun classify_produce_roots() {
        assertCategory("MANDIOCA FRESCA 1KG", ProductCategory.PRODUCE)
        assertCategory("AIPIM DESCASCADO", ProductCategory.PRODUCE)
        assertCategory("INHAME BRANCO", ProductCategory.PRODUCE)
    }

    // ========== AMBIGUOUS/EDGE CASES ==========

    @Test
    fun classify_ambiguous_paperVsCloth() {
        // P TOALHA should be CLEANING, not HYGIENE
        assertCategory("P TOALHA MILI 3X200F", ProductCategory.CLEANING)

        // TOALHA UMED should be HYGIENE
        assertCategory("TOALHA UMED HUGGIES L4P3 C/48UN", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_ambiguous_desodorantContext() {
        // DES with REXONA or DOVE -> HYGIENE
        assertCategory("DES REXONA CLINICAL MEN 150ML", ProductCategory.HYGIENE)
        assertCategory("DES DOVE", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_ambiguous_shampooBrand() {
        // SH with CLEAR or HEAD -> HYGIENE
        assertCategory("SH HEAD&SHOULDERS A.COC 650ML", ProductCategory.HYGIENE)
        assertCategory("SH CLEAR MEN LIMP PROF 400ML", ProductCategory.HYGIENE)
    }

    @Test
    fun classify_ambiguous_shortTokens_doNotGenerateFalsePositivesWithoutContext() {
        assertCategory("SH 250ML", ProductCategory.OTHER)
        assertCategory("DES 150ML", ProductCategory.OTHER)
        assertCategory("SAB 90G", ProductCategory.OTHER)
        assertCategory("ALE 600ML", ProductCategory.OTHER)
        assertCategory("VH 750ML", ProductCategory.OTHER)
        assertCategory("BEB MISTA", ProductCategory.OTHER)
    }

    @Test
    fun classify_ambiguous_shortTokens_matchWhenContextIsSafe() {
        assertCategory("VH AURORA C.SAUV 750ML", ProductCategory.BEVERAGES)
        assertCategory("DES REXONA CLINICAL MEN 150ML", ProductCategory.HYGIENE)
        assertCategory("SAB DOVE KARITE 90G", ProductCategory.HYGIENE)
        assertCategory("SH CLEAR MEN 400ML", ProductCategory.HYGIENE)
        assertCategory("AMAC COMFORT FRESCOR 1L", ProductCategory.CLEANING)
    }

    @Test
    fun classify_ambiguous_fleshTomatoVsProcessed() {
        // Pure TOMATE -> PRODUCE
        assertCategory("TOMATE ITALIANO 500G", ProductCategory.PRODUCE)

        // MOLHO TOMATE -> BASIC_FOOD
        assertCategory("MOLHO TOMATE HEINZ 350G", ProductCategory.BASIC_FOOD)
    }

    // ========== ITEMS THAT SHOULD REMAIN OTHER (non-food) ==========

    @Test
    fun classify_nonFood_shouldRemainOther() {
        // These items don't fit into food/personal care categories in V1
        assertCategory("TULIPA VASO 12", ProductCategory.OTHER)
        assertCategory("CANECA PLASUTIL 14468 MICK 360ML", ProductCategory.OTHER)
        assertCategory("FITA CREPE T.BOND FIXA 48X50M", ProductCategory.OTHER)
        assertCategory("CAD 1M 80F CREDEAL ESSENC 281635", ProductCategory.OTHER)
        assertCategory("PULVERIZ SANREMO 512 AZ 350ML", ProductCategory.OTHER)
        assertCategory("ALIM CAO PEDIGREE FIL 900G", ProductCategory.OTHER)  // Pet food
        assertCategory("BIFINHO PETHAND SENIOR CARN 65G", ProductCategory.OTHER)  // Pet food
    }

    // ========== TEST COVERAGE ==========

    @Test
    fun classifyAll_generatesClassificationSummary() {
        val items = listOf(
            ProductItem(name = "COCA-COLA 350ML", price = 5.0),
            ProductItem(name = "ARROZ 1KG", price = 8.0),
            ProductItem(name = "TOMATE", price = 3.0),
            ProductItem(name = "SABONETE DOVE", price = 2.5),
            ProductItem(name = "DETERGENTE 500ML", price = 3.0),
            ProductItem(name = "PIZZA SADIA", price = 12.0),
            ProductItem(name = "DESCONHECIDO XYZ", price = 99.0)  // This will be OTHER
        )

        val results = classifier.classifyAll(items)

        // Summary will be logged, we just ensure it doesn't crash
        assertEquals(7, results.size)

        // Check that at least 6 are not OTHER
        val otherCount = results.count { it.category == ProductCategory.OTHER }
        assertEquals(1, otherCount)
    }

    private fun assertCategory(name: String, expected: ProductCategory) {
        val item = ProductItem(name = name, price = 10.0)
        val result = classifier.classify(item)
        assertEquals("Failed for item: '$name'", expected, result.category)
    }
}

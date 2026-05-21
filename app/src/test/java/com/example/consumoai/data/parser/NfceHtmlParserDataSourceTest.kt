package com.example.consumoai.data.parser

import org.jsoup.Jsoup
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate

class NfceHtmlParserDataSourceTest {

    private val parser = NfceHtmlParserDataSource()

    @Test
    fun parse_extractsProductsFromRowsAndIgnoresFooter() {
        val html = """
            <html>
              <body>
                <table>
                  <tr><td>1 SUCO NATURALE 13,90</td></tr>
                  <tr><td>2 COCA-COLA ORIG 2L 10,93</td></tr>
                  <tr><td>VALOR TOTAL 24,83</td></tr>
                </table>
              </body>
            </html>
        """.trimIndent()

        val parsed = parser.parse(Jsoup.parse(html))
        val products = parsed.items

        assertEquals(2, products.size)
        assertEquals(1, products[0].itemNumber)
        assertEquals("SUCO NATURALE", products[0].name)
        assertEquals(13.90, products[0].price, 0.0001)
        assertEquals(2, products[1].itemNumber)
        assertEquals("COCA-COLA ORIG 2L", products[1].name)
        assertEquals(10.93, products[1].price, 0.0001)
    }

    @Test
    fun parse_extractsIssueDateWhenEmissionIsPresent() {
        val html = """
            <html>
              <body>
                Data de Emissao: 14/05/2026 18:45:11
                <table>
                  <tr><td>1 ARROZ 8,90</td></tr>
                </table>
              </body>
            </html>
        """.trimIndent()

        val parsed = parser.parse(Jsoup.parse(html))

        assertNotNull(parsed.issueDate)
        assertEquals(LocalDate.of(2026, 5, 14), parsed.issueDate)
    }
}


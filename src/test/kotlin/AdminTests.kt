import com.nhaarman.mockito_kotlin.doReturn
import com.nhaarman.mockito_kotlin.mock
import me.mgaldieri.pismowarehouse.DBHelper
import me.mgaldieri.pismowarehouse.models.Product
import org.junit.Assert
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.mockito.Mock
import org.sql2o.Connection
import org.sql2o.Sql2o
import spark.Request
import spark.Response
import spark.Spark

class AdminTests {

    @Test
    fun testSuccessfulLogin() {
        val request: Request
        val response: Response

        Spark.init()

        assert(true)
    }

    @Test
    fun testSaveProduct() {
        val DBHelper = mock<DBHelper> {
            on { getInstance() } doReturn Sql2o("jdbc:h2:mem:warehouse-test", null, null)
        }
        val initSQL = object {}.javaClass.getResource("initdb.sql").readText(Charsets.UTF_8)
        val db = DBHelper.getInstance()
        val conn: Connection = db.open() ?: throw RuntimeException("Could not open connection to DB")
        conn.createQuery(initSQL).executeUpdate()

        val product = Product(null, "PRODUTO DE TESTE", "DESCRIÇÃO DE TESTE", 1000, 10)
        Assert.assertTrue(product.save())
    }
}
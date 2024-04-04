import com.google.common.net.HttpHeaders.ACCEPT
import io.quarkus.test.junit.QuarkusTest
import io.restassured.RestAssured.*
import io.restassured.common.mapper.TypeRef
import jakarta.ws.rs.core.MediaType.APPLICATION_JSON
import org.apache.http.HttpStatus
import org.apache.http.HttpStatus.SC_CREATED
import org.hamcrest.Matchers.`is`
import org.jboss.resteasy.reactive.RestResponse.StatusCode.NO_CONTENT
import org.jboss.resteasy.reactive.RestResponse.StatusCode.OK
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder


@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
class TodoResourceTest {

    @Test
    @Order(1)
    fun testInitialItems() {
        val todos = get("/api")
            .then()
            .statusCode(OK)
            .extract()
            .body()
            .`as`(getTodoTypeRef())

        assertEquals(4, todos.size)

        get("/api/1")
            .then()
            .statusCode(OK)
            .contentType(APPLICATION_JSON)
            .body("title", `is`("Introduction to Quarkus"))
            .body("completed", `is`(true))
    }

    @Test
    @Order(2)
    fun testAddingAnItem() {
        val todo = Todo().apply {
            title = "testing the application"
        }

        given()
            .body(todo)
            .contentType(APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .`when`()
            .post("/api")
            .then()
            .statusCode(SC_CREATED)
            .contentType(APPLICATION_JSON)
            .body("title", `is`(todo.title))
            .body("completed", `is`(false))
            .body("id", `is`(5))

        val todos = get("/api")
            .then()
            .statusCode(OK)
            .extract()
            .body()
            .`as`(getTodoTypeRef())

        assertEquals(5, todos.size)
    }

    @Test
    @Order(3)
    fun testUpdatingAnItem() {
        val todo = Todo().apply {
            title = "testing the application (updated)"
            completed = true
        }

        given()
            .body(todo)
            .contentType(APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .pathParam("id", 5)
            .`when`()
            .patch("/api/{id}")
            .then()
            .statusCode(OK)
            .contentType(APPLICATION_JSON)
            .body("title", `is`(todo.title))
            .body("completed", `is`(true))
            .body("id", `is`(5))
    }

    @Test
    @Order(4)
    fun testDeletingAnItem() {
        given()
            .contentType(APPLICATION_JSON)
            .header(ACCEPT, APPLICATION_JSON)
            .pathParam("id", 5)
            .`when`()
            .delete("/api/{id}")
            .then()
            .statusCode(NO_CONTENT)

        val todos = get("/api")
            .then()
            .statusCode(OK)
            .extract()
            .body()
            .`as`(getTodoTypeRef())

        assertEquals(4, todos.size)
    }

    @Test
    @Order(5)
    fun testDeleteCompleted() {
        delete("/api")
            .then()
            .statusCode(NO_CONTENT)

        val todos = get("/api")
            .then()
            .statusCode(OK)
            .extract()
            .body()
            .`as`(getTodoTypeRef())

        assertEquals(3, todos.size)
    }

    private fun getTodoTypeRef(): TypeRef<List<Todo>> = object : TypeRef<List<Todo>>() {}

}
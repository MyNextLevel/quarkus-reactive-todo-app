import io.quarkus.hibernate.reactive.panache.Panache.withTransaction
import io.quarkus.panache.common.Sort
import io.smallrye.mutiny.Uni
import jakarta.validation.Valid
import jakarta.ws.rs.*
import jakarta.ws.rs.core.Response
import jakarta.ws.rs.core.Response.Status.CREATED
import jakarta.ws.rs.core.Response.Status.NOT_FOUND

@Path("/api")
class TodoResource {

    @GET
    fun getAll(): Uni<List<Todo>> =
        withTransaction {
            Todo.findAll(Sort.by("order")).list()
        }

    @GET
    @Path("/{id}")
    fun getOne(@PathParam("id") id: Long): Uni<Todo> =
        withTransaction {
            Todo.findById(id)
                .onItem().ifNull().failWith {
                    WebApplicationException(
                        "Todo with id of $id does not exist.",
                        NOT_FOUND
                    )
                }
        }

    @POST
    fun create(@Valid item: Todo): Uni<Response> =
        withTransaction(item::persist)
            .replaceWith { Response.status(CREATED).entity(item).build() }

    @PATCH
    @Path("/{id}")
    fun update(@Valid todo: Todo, @PathParam("id") id: Long): Uni<Todo?> = withTransaction {
        Todo.findById(id).onItem().transform { entity ->
            entity.apply {
                this?.completed = todo.completed
                this?.order = todo.order
                this?.title = todo.title
                this?.url = todo.url
            }
        }
    }

    @DELETE
    fun deleteCompleted(): Uni<Response> =
        withTransaction(Todo::deleteCompleted).replaceWith { Response.noContent().build() }

    @DELETE
    @Path("/{id}")
    fun deleteOne(@PathParam("id") id: Long): Uni<Response> =
        withTransaction {
            Todo
                .findById(id)
                .onItem()
                .ifNull()
                .failWith { WebApplicationException("Todo with id of $id does not exist.", NOT_FOUND) }
                .call { it -> it!!.delete() }
        }.replaceWith { Response.noContent().build() }

}
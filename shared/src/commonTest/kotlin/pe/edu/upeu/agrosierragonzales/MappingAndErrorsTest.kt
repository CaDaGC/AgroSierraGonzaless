package pe.edu.upeu.agrosierragonzales
import kotlin.test.*
import pe.edu.upeu.agrosierragonzales.data.remote.dto.*
import pe.edu.upeu.agrosierragonzales.data.remote.mapHttpError
import pe.edu.upeu.agrosierragonzales.domain.error.ApiError
class MappingAndErrorsTest{
 @Test fun dtoMapping(){val x=LoteDto(4,"C-4","Lote",2.0,"Café","Valle",false,5.0).toDomain();assertEquals(4L,x.id);assertEquals("Valle",x.valle)}
 @Test fun typedHttpErrors(){assertEquals(ApiError.Unauthorized,mapHttpError(401));assertIs<ApiError.Validation>(mapHttpError(400,"invalido"));assertEquals(ApiError.Forbidden,mapHttpError(403))}
}

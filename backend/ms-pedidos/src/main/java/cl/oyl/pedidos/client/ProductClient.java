package cl.oyl.pedidos.client;

import cl.oyl.pedidos.dto.ProductInfo;
import cl.oyl.pedidos.exception.BusinessException;
import cl.oyl.pedidos.exception.ResourceNotFoundException;
import cl.oyl.pedidos.exception.ServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Comunicación entre microservicios: ms-pedidos llama por HTTP a ms-productos
 * para leer el precio real del producto y descontar el stock.
 */
@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(@Value("${app.products.base-url}") String baseUrl) {
        this.restClient = RestClient.builder().baseUrl(baseUrl).build();
    }

    public ProductInfo getProduct(Long id) {
        try {
            return restClient.get().uri("/api/products/{id}", id)
                    .retrieve()
                    .body(ProductInfo.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResourceNotFoundException("Producto " + id + " no existe");
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("El servicio de productos no está disponible");
        }
    }

    public void decreaseStock(Long id, int quantity) {
        try {
            restClient.post().uri("/api/products/{id}/decrease-stock", id)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("quantity", quantity))
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.CONFLICT) {
                throw new BusinessException("Stock insuficiente para el producto " + id);
            }
            throw new BusinessException("No se pudo descontar el stock del producto " + id);
        } catch (ResourceAccessException e) {
            throw new ServiceUnavailableException("El servicio de productos no está disponible");
        }
    }
}

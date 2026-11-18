package cl.oyl.productos.service;

import cl.oyl.productos.dto.ProductRequest;
import cl.oyl.productos.exception.BusinessException;
import cl.oyl.productos.exception.ResourceNotFoundException;
import cl.oyl.productos.model.Product;
import cl.oyl.productos.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Pruebas unitarias del servicio: el repositorio se reemplaza por un mock de Mockito. */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    private Product nuez;

    @BeforeEach
    void setUp() {
        nuez = new Product();
        nuez.setId(1L);
        nuez.setName("Nueces Mariposa");
        nuez.setCategory("Frutos Secos");
        nuez.setPrice(9900.0);
        nuez.setStock(10);
        nuez.setUnit("kg");
    }

    private ProductRequest request(String barcode) {
        return new ProductRequest(" Almendras ", "Frutos Secos", 11500.0, 5, null,
                null, null, null, barcode, null, null);
    }

    @Test
    void findAll_sinFiltros_devuelveTodos() {
        when(repository.findAll()).thenReturn(List.of(nuez));
        assertThat(service.findAll(null, null)).containsExactly(nuez);
    }

    @Test
    void findAll_conBusqueda_usaConsultaPorNombre() {
        when(repository.findByNameContainingIgnoreCaseOrderByNameAsc("nuez")).thenReturn(List.of(nuez));
        assertThat(service.findAll(null, " nuez ")).hasSize(1);
        verify(repository, never()).findAll();
    }

    @Test
    void findAll_categoriaTodos_devuelveTodos() {
        when(repository.findAll()).thenReturn(List.of(nuez));
        service.findAll("Todos", null);
        verify(repository).findAll();
    }

    @Test
    void findAll_conCategoria_filtraPorCategoria() {
        when(repository.findByCategoryIgnoreCaseOrderByNameAsc("Semillas")).thenReturn(List.of());
        assertThat(service.findAll("Semillas", null)).isEmpty();
    }

    @Test
    void findById_inexistente_lanza404() {
        when(repository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> service.findById(99L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByBarcode_existente_devuelveProducto() {
        when(repository.findByBarcode("780998810001")).thenReturn(Optional.of(nuez));
        assertThat(service.findByBarcode("780998810001").getName()).isEqualTo("Nueces Mariposa");
    }

    @Test
    void categories_cuentaProductosPorCategoria() {
        Product semilla = new Product();
        semilla.setCategory("Semillas");
        when(repository.findAll()).thenReturn(List.of(nuez, semilla, nuez));
        assertThat(service.categories())
                .extracting("name", "productCount")
                .containsExactly(tuple("Frutos Secos", 2L), tuple("Semillas", 1L));
    }

    @Test
    void create_aplicaValoresPorDefectoYLimpiaTexto() {
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        Product creado = service.create(request(""));
        assertThat(creado.getName()).isEqualTo("Almendras");
        assertThat(creado.getUnit()).isEqualTo("kg");
        assertThat(creado.getOrigin()).isEqualTo("Chile");
        assertThat(creado.getBarcode()).isNull();
    }

    @Test
    void create_codigoDuplicado_lanza409() {
        when(repository.existsByBarcode("780998810001")).thenReturn(true);
        assertThatThrownBy(() -> service.create(request("780998810001")))
                .isInstanceOf(BusinessException.class);
        verify(repository, never()).save(any());
    }

    @Test
    void update_modificaProductoExistente() {
        when(repository.findById(1L)).thenReturn(Optional.of(nuez));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        Product actualizado = service.update(1L, request("780998810099"));
        assertThat(actualizado.getPrice()).isEqualTo(11500.0);
        assertThat(actualizado.getBarcode()).isEqualTo("780998810099");
    }

    @Test
    void update_codigoDeOtroProducto_lanza409() {
        when(repository.findById(1L)).thenReturn(Optional.of(nuez));
        when(repository.existsByBarcodeAndIdNot("780998810002", 1L)).thenReturn(true);
        assertThatThrownBy(() -> service.update(1L, request("780998810002")))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void delete_inexistente_lanza404() {
        when(repository.existsById(5L)).thenReturn(false);
        assertThatThrownBy(() -> service.delete(5L)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_existente_elimina() {
        when(repository.existsById(1L)).thenReturn(true);
        service.delete(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void updateStock_guardaNuevoValor() {
        when(repository.findById(1L)).thenReturn(Optional.of(nuez));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.updateStock(1L, 40).getStock()).isEqualTo(40);
    }

    @Test
    void decreaseStock_conStockSuficiente_descuenta() {
        when(repository.findById(1L)).thenReturn(Optional.of(nuez));
        when(repository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));
        assertThat(service.decreaseStock(1L, 3).getStock()).isEqualTo(7);
    }

    @Test
    void decreaseStock_sinStockSuficiente_lanza409() {
        when(repository.findById(1L)).thenReturn(Optional.of(nuez));
        assertThatThrownBy(() -> service.decreaseStock(1L, 11))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Stock insuficiente");
    }
}

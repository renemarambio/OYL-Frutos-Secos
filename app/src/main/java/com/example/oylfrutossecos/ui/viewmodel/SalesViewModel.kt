package com.example.oylfrutossecos.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.oylfrutossecos.data.model.CartItem
import com.example.oylfrutossecos.data.model.Sale
import com.example.oylfrutossecos.data.repository.SalesRepository
import com.example.oylfrutossecos.domain.validation.Validators
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutFormState(
    val customerName: String = "",
    val customerEmail: String = "",
    val customerPhone: String = "",
    val customerAddress: String = "",
    val deliveryZone: String = "Santiago Centro & Providencia",
    val paymentMethod: String = "Tarjeta Débito/Crédito",
    val nameError: String? = null,
    val emailError: String? = null,
    val phoneError: String? = null,
    val addressError: String? = null,
    val isProcessing: Boolean = false,
    val lastCompletedSale: Sale? = null
)

class SalesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = SalesRepository(application)

    private val _sales = MutableStateFlow<List<Sale>>(emptyList())
    val sales: StateFlow<List<Sale>> = _sales.asStateFlow()

    private val _selectedSale = MutableStateFlow<Sale?>(null)
    val selectedSale: StateFlow<Sale?> = _selectedSale.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _checkoutForm = MutableStateFlow(CheckoutFormState())
    val checkoutForm: StateFlow<CheckoutFormState> = _checkoutForm.asStateFlow()

    init {
        loadSales()
    }

    fun loadSales() {
        viewModelScope.launch {
            _isLoading.value = true
            _sales.value = repository.getAllSales()
            _isLoading.value = false
        }
    }

    fun selectSale(sale: Sale?) {
        _selectedSale.value = sale
    }

    fun onCheckoutFieldChanged(
        name: String? = null,
        email: String? = null,
        phone: String? = null,
        address: String? = null,
        deliveryZone: String? = null,
        paymentMethod: String? = null
    ) {
        _checkoutForm.update { current ->
            current.copy(
                customerName = name ?: current.customerName,
                customerEmail = email ?: current.customerEmail,
                customerPhone = phone ?: current.customerPhone,
                customerAddress = address ?: current.customerAddress,
                deliveryZone = deliveryZone ?: current.deliveryZone,
                paymentMethod = paymentMethod ?: current.paymentMethod,
                nameError = if (name != null) null else current.nameError,
                emailError = if (email != null) null else current.emailError,
                phoneError = if (phone != null) null else current.phoneError,
                addressError = if (address != null) null else current.addressError
            )
        }
    }

    fun confirmOrder(
        cartItems: List<CartItem>,
        deliveryFee: Double,
        onSuccess: (Sale) -> Unit,
        onError: (String) -> Unit
    ) {
        val form = _checkoutForm.value
        // Validación centralizada en domain/validation/Validators
        val nameErr = Validators.validatePersonName(form.customerName)
        val emailErr = Validators.validateEmail(form.customerEmail)
        val phoneErr = Validators.validateChileanPhone(form.customerPhone)
        val addrErr = Validators.validateAddress(form.customerAddress)

        if (listOf(nameErr, emailErr, phoneErr, addrErr).any { it != null }) {
            _checkoutForm.update {
                it.copy(nameError = nameErr, emailError = emailErr, phoneError = phoneErr, addressError = addrErr)
            }
            return
        }

        viewModelScope.launch {
            _checkoutForm.update { it.copy(isProcessing = true) }
            val result = repository.createSale(
                cartItems = cartItems,
                customerName = form.customerName.trim(),
                customerEmail = form.customerEmail.trim(),
                customerAddress = form.customerAddress.trim(),
                deliveryZone = form.deliveryZone,
                deliveryFee = deliveryFee,
                paymentMethod = form.paymentMethod
            )

            result.onSuccess { sale ->
                // Se limpia el formulario para la siguiente venta
                _checkoutForm.update { CheckoutFormState(lastCompletedSale = sale) }
                loadSales()
                onSuccess(sale)
            }.onFailure { error ->
                _checkoutForm.update { it.copy(isProcessing = false) }
                onError(error.message ?: "Ocurrió un error al procesar la venta")
            }
        }
    }
}

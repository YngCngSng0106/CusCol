import http from '../utils/request'

export function pageContracts(params) {
  return http.get('/contract/page', { params })
}

export function listCustomerContracts(customerId) {
  return http.get(`/contract/customer/${customerId}`)
}

export function getContract(id) {
  return http.get(`/contract/${id}`)
}

export function createContract(data) {
  return http.post('/contract', data)
}

export function updateContract(id, data) {
  return http.put(`/contract/${id}`, data)
}

export function deleteContract(id) {
  return http.delete(`/contract/${id}`)
}

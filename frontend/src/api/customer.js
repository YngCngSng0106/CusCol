import http from '../utils/request'

export function pageCustomers(params) {
  return http.get('/customer/page', { params })
}

export function createCustomer(data) {
  return http.post('/customer', data)
}

export function getCustomer(id) {
  return http.get(`/customer/${id}`)
}

export function updateCustomer(id, data) {
  return http.put(`/customer/${id}`, data)
}

export function setCustomerValid(id, izValid) {
  return http.put(`/customer/${id}/valid`, null, { params: { izValid } })
}

export function deleteCustomer(id) {
  return http.delete(`/customer/${id}`)
}

export function listProjects(customerId) {
  return http.get(`/customer/${customerId}/project/list`)
}

export function addProject(customerId, data) {
  return http.post(`/customer/${customerId}/project`, data)
}

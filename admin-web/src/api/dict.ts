import request from '../utils/request'

export interface DictTypeItem {
  id: number
  dictCode: string
  dictName: string
  sort: number
  status: number
}

export interface DictTypeCreateDTO {
  dictCode: string
  dictName: string
  sort: number
  status: number
}

export interface DictTypeUpdateDTO {
  dictName: string
  sort: number
  status: number
}

export interface DictDataItem {
  id: number
  dictTypeId: number
  dictLabel: string
  dictValue: string
  sort: number
  status: number
  remark?: string
}

export interface DictDataCreateDTO {
  dictTypeId: number
  dictLabel: string
  dictValue: string
  sort: number
  status: number
  remark?: string
}

export interface DictDataUpdateDTO {
  dictLabel: string
  sort: number
  status: number
  remark?: string
}

export const getDictTypeList = (params?: { keyword?: string }) => {
  return request.get<DictTypeItem[]>('/dict-types', { params })
}

export const getDictTypeOptions = () => {
  return request.get<DictTypeItem[]>('/dict-types/options')
}

export const createDictType = (data: DictTypeCreateDTO) => {
  return request.post<number>('/dict-types', data)
}

export const updateDictType = (id: number, data: DictTypeUpdateDTO) => {
  return request.put<number>(`/dict-types/${id}`, data)
}

export const deleteDictType = (id: number) => {
  return request.delete<number>(`/dict-types/${id}`)
}

export const getDictDataList = (params: { dictTypeId: number; keyword?: string }) => {
  return request.get<DictDataItem[]>('/dict-data', { params })
}

export const getDictDataOptions = (dictCode: string) => {
  return request.get<DictDataItem[]>('/dict-data/options', { params: { dictCode } })
}

export const createDictData = (data: DictDataCreateDTO) => {
  return request.post<number>('/dict-data', data)
}

export const updateDictData = (id: number, data: DictDataUpdateDTO) => {
  return request.put<number>(`/dict-data/${id}`, data)
}

export const deleteDictData = (id: number) => {
  return request.delete<number>(`/dict-data/${id}`)
}

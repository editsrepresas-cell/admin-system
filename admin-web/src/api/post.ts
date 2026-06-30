import request from '../utils/request'

export interface PostItem {
  id: number
  postCode: string
  postName: string
  sort: number
  status: number
  createTime?: string
}

export interface PostCreateDTO {
  postCode: string
  postName: string
  sort: number
  status: number
}

export interface PostUpdateDTO {
  postName: string
  sort: number
  status: number
}

export const getPostList = () => {
  return request.get<PostItem[]>('/posts')
}

export const getPostOptions = () => {
  return request.get<PostItem[]>('/posts/options')
}

export const createPost = (data: PostCreateDTO) => {
  return request.post<number>('/posts', data)
}

export const updatePost = (id: number, data: PostUpdateDTO) => {
  return request.put<number>(`/posts/${id}`, data)
}

export const deletePost = (id: number) => {
  return request.delete<number>(`/posts/${id}`)
}

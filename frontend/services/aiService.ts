import api from './api';

export interface ChatRequest {
  message: string;
  context?: { currentPage?: string };
}

export interface ChatResponse {
  message: string;
}

export const aiService = {
  chat: async (request: ChatRequest): Promise<ChatResponse> => {
    const response = await api.post<{ data: ChatResponse }>('/ai/chat', request);
    return response.data.data;
  },
};

export default aiService;

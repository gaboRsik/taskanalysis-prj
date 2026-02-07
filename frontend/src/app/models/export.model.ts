export enum ExportFormat {
  XLSX = 'XLSX',
  PDF = 'PDF'
}

export enum DeliveryMethod {
  DOWNLOAD = 'DOWNLOAD',
  EMAIL = 'EMAIL'
}

export interface ExportRequest {
  format: ExportFormat;
  delivery: DeliveryMethod;
}

export interface ExportResponse {
  success: boolean;
  message: string;
  deliveryMethod: DeliveryMethod;
  fileName: string;
}


export type Language = 'ko' | 'en';

export type OrderStatus = 
  | 'NEW' | 'PAYMENT_PENDING' | 'PAID' | 'PREPARING' 
  | 'READY_TO_SHIP' | 'SHIPPED' | 'IN_DELIVERY' | 'DELIVERED' 
  | 'CANCELLED' | 'EXCHANGE_REQUESTED' | 'RETURN_REQUESTED';

export type StockStatus = 'NORMAL' | 'LOW' | 'OUT_OF_STOCK' | 'OVERSTOCK';

export interface CustomsStrategy {
  countryCode: string;
  localHsCode: string;
  invoiceName: string;
  dutyRate: string;
  requiredDocs: string[];
  complianceAlert?: string;
}

export interface BarcodeInfo {
  code: string;
  isMain: boolean;
}

export interface Product {
  id: string;
  sku: string;
  name: { [key: string]: string };
  brand: string;
  category: string;
  uom: 'PCS' | 'SET' | 'BOX' | 'KG' | '台' | 'EA';
  color?: string;
  ownerName?: string;
  customerGoodsNo?: string;
  barcodes: BarcodeInfo[]; // 다중 바코드 객체 구조
  basePrice: number;
  totalStock: number;
  status: 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK';
  dimensions: {
    width: number;
    length: number;
    height: number;
    unit: 'cm' | 'mm';
  };
  netWeight: number;
  grossWeight: number;
  logisticsInfo: {
    tempMgmt: 'Normal' | 'Temperature Control' | 'Cold' | 'Freezing' | 'Cryogenic';
    shelfLifeMgmt: boolean;
    snMgmt: boolean;
    isDangerous: boolean;
    isFragile: boolean;
    isHighValue: boolean;
    isNonStandard: boolean;
  };
  hsCode: string;
  countryOfOrigin: string;
  materialContent: string;
  manufacturerDetails: {
    name: string;
    address: string;
  };
  customsStrategies: CustomsStrategy[];
}

export interface Inventory {
  productId: string;
  productName: string;
  warehouse: string;
  total: number;
  available: number;
  reserved: number;
  safetyStock: number;
  status: StockStatus;
  channelBreakdown: Record<string, number>;
}

export interface Order {
  id: string;
  channel: string;
  orderDate: string;
  customerName: string;
  totalAmount: number;
  status: OrderStatus;
  fulfillmentMethod?: 'WMS' | 'DIRECT'; // 배송 주체 구분
  wmsNode?: string; 
  routingLogic?: string;
  items: Array<{
    productId: string;
    productName: string;
    quantity: number;
    price: number;
  }>;
}

export interface Channel {
  id: string;
  name: string;
  type: string;
  status: 'CONNECTED' | 'DISCONNECTED' | 'ERROR';
  lastSync: string;
  logo: string;
}

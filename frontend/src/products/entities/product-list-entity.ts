export interface ProductListEntity {
  readonly id: string;
  readonly name: string;
  readonly ean: string;
  readonly category: string;
  readonly mainImageUrl: string;
  readonly lowestCurrentPrice: number;
  readonly lowestPriceShop: string;
  readonly offerCount: number;
  readonly isAvailable: boolean;
}

export interface PriceStampEntity {
  readonly timestamp: string;
  readonly price: number;
  readonly currency: string;
  readonly promoCode: string | null;
  readonly isAvailable: boolean;
  readonly condition: string;
}

export interface OfferEntity {
  readonly shop: string;
  readonly shopLogoUrl: string;
  readonly url: string;
  readonly priceHistory: PriceStampEntity[];
}

export interface ProductDetailEntity {
  readonly id: string;
  readonly ean: string;
  readonly name: string;
  readonly category: string;
  readonly images: string[];
  readonly offers: OfferEntity[];
}

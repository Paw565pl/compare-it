import { PriceStampEntity } from "@/products/entities/price-stamp-entity";
import { ShopEntity } from "@/products/entities/shop-entity";

export interface OfferEntity {
  readonly shop: ShopEntity;
  readonly url: string;
  readonly priceHistory: PriceStampEntity[];
  readonly isAvailable: boolean;
}

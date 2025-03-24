import { CategoryEntity } from "@/products/entities/category-entity";
import { ShopEntity } from "@/products/entities/shop-entity";

export interface ProductListEntity {
  readonly id: string;
  readonly name: string;
  readonly ean: string;
  readonly category: CategoryEntity;
  readonly mainImageUrl: string | null;
  readonly lowestCurrentPrice: number | null;
  readonly lowestPriceCurrency: string | null;
  readonly lowestPriceShop: ShopEntity | null;
  readonly offersCount: number;
  readonly isAvailable: boolean;
}

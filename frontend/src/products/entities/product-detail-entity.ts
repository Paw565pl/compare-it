import { CategoryEntity } from "@/products/entities/category-entity";
import { OfferEntity } from "@/products/entities/offer-entity";

export interface ProductDetailEntity {
  readonly id: string;
  readonly ean: string;
  readonly name: string;
  readonly category: CategoryEntity;
  readonly images: string[];
  readonly offers: OfferEntity[];
}

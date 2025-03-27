import { OfferEntity } from "@/products/entities/offer-entity";

export interface ProductDetailEntity {
  readonly id: string;
  readonly ean: string;
  readonly name: string;
  readonly category: string;
  readonly images: string[];
  readonly offers: OfferEntity[];
}

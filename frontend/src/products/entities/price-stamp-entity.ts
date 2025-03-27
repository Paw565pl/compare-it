import { ConditionEntity } from "@/products/entities/condition-entity";

export interface PriceStampEntity {
  readonly timestamp: string;
  readonly price: number;
  readonly currency: string;
  readonly promoCode: string | null;
  readonly condition: ConditionEntity;
}

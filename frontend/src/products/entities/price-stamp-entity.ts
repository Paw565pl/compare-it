import { ConditionEntity } from "@/products/entities/condition-entity";
import { CurrencyEntity } from "@/products/entities/currency-entity";

export interface PriceStampEntity {
  readonly timestamp: string;
  readonly price: number;
  readonly currency: CurrencyEntity;
  readonly promoCode: string | null;
  readonly condition: ConditionEntity;
}

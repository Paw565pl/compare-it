export enum ConditionEntity {
  NEW = "NEW",
  OUTLET = "OUTLET",
}

export const conditionDisplayNameMap: Record<ConditionEntity, string> = {
  [ConditionEntity.NEW]: "Nowy",
  [ConditionEntity.OUTLET]: "Outlet",
} as const;

export enum ConditionEntity {
  NEW = "NEW",
  OUTLET = "OUTLET",
}

export const conditionsHumanReadableNames: Record<ConditionEntity, string> = {
  [ConditionEntity.NEW]: "Nowy",
  [ConditionEntity.OUTLET]: "Outlet",
} as const;

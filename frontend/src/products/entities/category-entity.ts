export enum CategoryEntity {
  CPU = "CPU",
  GPU = "GPU",
  MOTHERBOARD = "MOTHERBOARD",
  RAM_MEMORY = "RAM_MEMORY",
  SSD_DRIVE = "SSD_DRIVE",
  POWER_SUPPLY = "POWER_SUPPLY",
  PC_CASE = "PC_CASE",
}

export const categoryDisplayNameMap: Record<CategoryEntity, string> = {
  [CategoryEntity.CPU]: "Procesory",
  [CategoryEntity.GPU]: "Karty graficzne",
  [CategoryEntity.MOTHERBOARD]: "Płyty główne",
  [CategoryEntity.RAM_MEMORY]: "Pamięci RAM",
  [CategoryEntity.SSD_DRIVE]: "Dyski SSD",
  [CategoryEntity.POWER_SUPPLY]: "Zasilacze",
  [CategoryEntity.PC_CASE]: "Obudowy",
} as const;

export const categoryByDisplayNameMap: Record<string, CategoryEntity> =
  Object.fromEntries(
    Object.entries(categoryDisplayNameMap).map(([k, v]) => [
      v,
      k as CategoryEntity,
    ]),
  );

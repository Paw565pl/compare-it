"use client";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectOption,
  SelectTrigger,
  SelectValue,
} from "@/core/components/ui/select";
import { productPaginationSearchParams } from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

const sortOptions: SelectOption[] = [
  {
    label: "Domyślne",
    value: "default",
  },
  {
    label: "Nazwa (A-Z)",
    value: "name",
  },
  {
    label: "Nazwa (Z-A)",
    value: "name,desc",
  },
  {
    label: "Cena (od najniższej)",
    value: "lowestCurrentPrice",
  },
  {
    label: "Cena (od najwyższej)",
    value: "lowestCurrentPrice,desc",
  },
  {
    label: "Liczba ofert (rosnąco)",
    value: "offersCount",
  },
  {
    label: "Liczba ofert (malejąco)",
    value: "offersCount,desc",
  },
] as const;

export const SortSelect = () => {
  const [{ sort }, setProductPagination] = useQueryStates(
    productPaginationSearchParams,
  );

  const handleSortChange = (sortValue: string) => {
    const newSort = sortValue !== "default" ? sortValue : null;
    setProductPagination((prev) => ({ ...prev, page: 1, sort: newSort }));
  };

  return (
    <Select value={sort ?? ""} onValueChange={handleSortChange}>
      <SelectTrigger aria-label="Sortuj" className="xs:w-48 w-40">
        <SelectValue placeholder="SORTUJ" />
      </SelectTrigger>
      <SelectContent>
        {sortOptions.map(({ label, value }, index) => (
          <SelectItem key={index} value={value}>
            {label}
          </SelectItem>
        ))}
      </SelectContent>
    </Select>
  );
};

"use client";

import {
  Select,
  SelectContent,
  SelectGroup,
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
];

export const SortSelect = () => {
  const [{ sort }, setPagination] = useQueryStates(
    productPaginationSearchParams,
  );

  const handleSortChange = (sortValue: string) => {
    const newSort = sortValue !== "default" ? sortValue : null;
    setPagination((prev) => ({ ...prev, page: 1, sort: newSort }));
  };

  return (
    <Select value={sort ?? undefined} onValueChange={handleSortChange}>
      <SelectTrigger className="w-48">
        <SelectValue placeholder="SORTUJ" />
      </SelectTrigger>
      <SelectContent>
        <SelectGroup>
          {sortOptions.map(({ label, value }, index) => (
            <SelectItem key={index} value={value}>
              {label}
            </SelectItem>
          ))}
        </SelectGroup>
      </SelectContent>
    </Select>
  );
};

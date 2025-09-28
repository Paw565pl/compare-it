"use client";

import {
  Select,
  SelectContent,
  SelectItem,
  SelectOption,
  SelectTrigger,
  SelectValue,
} from "@/core/components/ui/select";
import { cn } from "@/core/utils/cn";
import { productPaginationSearchParams } from "@/products/search-params/product-search-params";
import { useQueryStates } from "nuqs";

interface SortSelectProps {
  readonly triggerClassName?: string;
}

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
    value: "availableOffersCount",
  },
  {
    label: "Liczba ofert (malejąco)",
    value: "availableOffersCount,desc",
  },
] as const;

export const SortSelect = ({ triggerClassName }: SortSelectProps) => {
  const [{ sort }, setProductPagination] = useQueryStates(
    productPaginationSearchParams,
  );

  const handleSortChange = (sortValue: string) => {
    const newSort = sortValue !== "default" ? sortValue : null;
    setProductPagination((prev) => ({ ...prev, page: 1, sort: newSort }));
  };

  return (
    <Select value={sort ?? ""} onValueChange={handleSortChange}>
      <SelectTrigger
        aria-label="Sortuj"
        className={cn("xs:w-48 w-40", triggerClassName)}
      >
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

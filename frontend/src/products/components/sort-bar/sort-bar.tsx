"use client";

import { Button } from "@/core/components/ui/button";
import { productPaginationSearchParams } from "@/products/search-params/product-search-params";
import { ChevronDown } from "lucide-react";
import { useQueryStates } from "nuqs";
import { useState } from "react";

const sortOptions = {
  Domyślne: "default",
  "Cena rosnąco": "lowestCurrentPrice",
  "Cena malejąco": "lowestCurrentPrice,desc",
  "Nazwa a-z": "name",
  "Nazwa z-a": "name,desc",
  "Liczba ofert rosnąco": "offersCount",
  "Liczba ofert malejąco": "offersCount,desc",
};

const SortBar = () => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [, setPagination] = useQueryStates(productPaginationSearchParams);

  const toggleDropdown = () => {
    setIsDropdownOpen((prev) => !prev);
  };

  const handleSortChange = (sortValue: string) => {
    const sort = sortValue !== "default" ? sortValue : null;

    setPagination((prev) => ({ ...prev, page: 1, sort }));
    setIsDropdownOpen(false);
  };

  return (
    <div className="text-md relative items-center">
      <Button variant="sort" onClick={toggleDropdown}>
        SORTUJ
        <ChevronDown className="w-6" />
      </Button>

      {isDropdownOpen && (
        <ul className="absolute left-0 mt-2 border bg-white shadow-lg">
          {Object.entries(sortOptions).map(([sortLabel, sortValue], index) => (
            <li key={index}>
              <Button
                variant="sortCategory"
                onClick={() => handleSortChange(sortValue)}
              >
                {sortLabel}
              </Button>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export { SortBar };

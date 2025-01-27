"use client";

import { ChevronDown } from "lucide-react";
import { useQueryStates } from "nuqs";
import { useState } from "react";

const sortOptions = {
  Domyślne: "default",
  "Cena rosnąco": "lowestCurrentPrice",
  "Cena malejąco": "lowestCurrentPrice,desc",
  "Nazwa a-z": "name",
  "Nazwa z-a": "name,desc",
  "Liczba ofert rosnąco": "offerCount",
  "Liczba ofert malejąco": "offerCount,desc",
};

const SortBar = () => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [_, setSelectedSort] = useState("Domyślne");
  const [_, setSort] = useQueryStates({ sort: "Domyślne" });

  const toggleDropdown = () => {
    setIsDropdownOpen((prev) => !prev);
  };

  const handleSortChange = (option, param) => {
    setSelectedSort(option);
    setSort({ sort: param });
    setIsDropdownOpen(false);
  };

  return (
    <div className="text-md relative items-center">
      <button
        onClick={toggleDropdown}
        className="flex bg-secondary px-4 py-2 text-white transition-colors hover:bg-hover"
      >
        SORTUJ
        <ChevronDown className="w-6" />
      </button>

      {isDropdownOpen && (
        <ul className="absolute left-0 mt-2 border bg-white shadow-lg">
          {Object.entries(sortOptions).map(([key, value]) => (
            <li
              key={value}
              onClick={() => handleSortChange(key, value)}
              className="cursor-pointer px-4 py-2 hover:bg-hover hover:text-white"
            >
              {key}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export { SortBar };

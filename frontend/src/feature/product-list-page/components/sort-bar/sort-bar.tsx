"use client";

import { ChevronDown } from "lucide-react";
import { useQueryStates } from "nuqs";
import { useState } from "react";

const SortBar = () => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedSort, setSelectedSort] = useState("Domyślne");
  const [_, setSort] = useQueryStates({ sort: "Domyślne" });

  const toggleDropdown = () => {
    setIsDropdownOpen((prev) => !prev);
  };

  const handleSortChange = (option, param) => {
    setSelectedSort(option);
    setSort({ sort: param });
    setIsDropdownOpen(false);
  };

  const sortParams = [
    "default",
    "lowestCurrentPrice",
    "lowestCurrentPrice,desc",
    "name",
    "name,desc",
    "offerCount",
    "offerCount,desc",
  ];
  const sortOptions = [
    "Domyślne",
    "Cena rosnąco",
    "Cena malejąco",
    "Nazwa a-z",
    "Nazwa z-a",
    "Liczba ofert rosnąco",
    "Liczba ofert malejąco",
  ];

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
          {sortOptions.map((option, index) => (
            <li
              key={index}
              onClick={() => handleSortChange(option, sortParams[index])}
              className="cursor-pointer px-4 py-2 hover:bg-hover hover:text-white"
            >
              {option}
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export { SortBar };

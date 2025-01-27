"use client";

import { ChevronDown } from "lucide-react";
import { useQueryStates } from "nuqs";
import { useState } from "react";

const SortBar = () => {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedSort, setSelectedSort] = useState("Domyślne");
  const [sort, setSort] = useQueryStates({ sort: "lowestCurrentPrice" });

  const toggleDropdown = () => {
    setIsDropdownOpen((prev) => !prev);
  };

  const handleSortChange = (option) => {
    setSelectedSort(option);
    setSort({ sort: option });
    setIsDropdownOpen(false);
  };

  const sortOptions = ["Domyślne", "lowestCurrentPrice"];

  return (
    <div className="text-md relative items-center">
      <button
        onClick={toggleDropdown}
        className="flex bg-secondary px-4 py-2 text-white transition-colors hover:bg-hover"
      >
        {selectedSort}
        <ChevronDown className="w-6" />
      </button>

      {isDropdownOpen && (
        <ul className="absolute left-0 mt-2 border bg-white shadow-lg">
          {sortOptions.map((option, index) => (
            <button
              key={index}
              onClick={() => handleSortChange(option)}
              className="cursor-pointer px-4 py-2 hover:bg-hover hover:text-white"
            >
              {option}
            </button>
          ))}
        </ul>
      )}
    </div>
  );
};

export { SortBar };

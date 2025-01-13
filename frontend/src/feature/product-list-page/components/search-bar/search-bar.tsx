import { Search } from "lucide-react";
import Link from "next/link";

const SearchBar = () => {
  return (
    <div className="mb-8 flex w-full justify-center">
      <input
        type="text"
        className="w-full rounded-l-lg px-4 py-2 text-lg focus:border-transparent focus:outline-none focus:ring-2 focus:ring-blue-500 md:w-1/3"
        placeholder="Wyszukaj wymarzony produkt"
      />
      <Link href={"/filter"}>
        <div className="flex items-center rounded-r-lg bg-blue-700 p-3 px-4 font-semibold text-white transition-colors duration-300 hover:bg-blue-800">
          <Search className="text-lg" />
          <div className="ml-2">Wyszukaj</div>
        </div>
      </Link>
    </div>
  );
};

export { SearchBar };

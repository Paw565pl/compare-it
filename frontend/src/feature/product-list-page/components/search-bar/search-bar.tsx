import { Search } from "lucide-react";
import Link from "next/link";

const SearchBar = () => {
  return (
    <div className="m-8 flex w-full justify-center">
      <input
        type="text"
        className="w-1/3 rounded-l-lg px-2"
        placeholder="Wyszukaj wymarzony produkt"
      />
      <Link href={"/filter"}>
        <div className="flex rounded-r-lg bg-blue-700 p-1 px-2 font-semibold text-white hover:bg-blue-800">
          <Search />
          <div className="ml-1">Wyszukaj</div>
        </div>
      </Link>
    </div>
  );
};

export { SearchBar };

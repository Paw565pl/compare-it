import { Button } from "@/core/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/core/components/ui/dialog";
import { H1 } from "@/core/components/ui/h1";
import { CategoriesList, FiltersBar, SortSelect } from "@/products/components";

export const ProductListPageHeader = () => {
  return (
    <header className="mt-5 mb-1 flex justify-between lg:mt-0">
      <H1 className="text-primary mb-0">Produkty</H1>

      <Dialog>
        <DialogTrigger asChild>
          <Button className="lg:hidden">FILTRUJ I SORTUJ</Button>
        </DialogTrigger>
        <DialogContent className="max-h-5/6 gap-0 overflow-y-scroll px-4">
          <DialogHeader className="mb-4">
            <DialogTitle>Filtry i sortowanie</DialogTitle>
            <DialogDescription>
              Dostosuj ustawienia filtrów i sortowania.
            </DialogDescription>
          </DialogHeader>

          <SortSelect triggerClassName="w-full! mb-2" />
          <CategoriesList />
          <FiltersBar />
        </DialogContent>
      </Dialog>

      <SortSelect triggerClassName="hidden lg:flex" />
    </header>
  );
};

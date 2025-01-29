import { Button } from "@/core/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/core/components/ui/dialog";
import { Input } from "@/core/components/ui/input";
import { Label } from "@/core/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/core/components/ui/select";
import { ReactNode } from "react";

interface PriceAlertFormDialogProps {
  dialogTrigger: ReactNode;
  dialogHeader: string;
  // TODO: use proper data type
  handleSubmit: (data: FormData) => void;
}

export const PriceAlertFormDialog = ({
  dialogTrigger,
  dialogHeader,
  handleSubmit,
}: PriceAlertFormDialogProps) => {
  return (
    <Dialog>
      <DialogTrigger asChild>{dialogTrigger}</DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{dialogHeader}</DialogTitle>
        </DialogHeader>
        <form onSubmit={() => handleSubmit(new FormData())}>
          <div className="grid gap-4 py-4">
            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="desiredCondition" className="text-right">
                Stan
              </Label>
              <Select defaultValue="new">
                <SelectTrigger id="desiredCondition" className="col-span-3">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="new">Nowy</SelectItem>
                  <SelectItem value="outlet">Outlet</SelectItem>
                </SelectContent>
              </Select>
            </div>

            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="desiredPrice" className="text-right">
                Cena
              </Label>
              <Input
                id="desiredPrice"
                defaultValue="200"
                className="col-span-3"
              />
            </div>
          </div>

          <DialogFooter>
            <Button type="submit">Zapisz zmiany</Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
};

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
  dialogTriggerLabel: ReactNode;
  // TODO: accept optionl prop for edited object
}

export const PriceAlertFormDialog = ({
  dialogTriggerLabel,
}: PriceAlertFormDialogProps) => {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button>{dialogTriggerLabel}</Button>
      </DialogTrigger>
      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>Edytuj alert cenowy</DialogTitle>
        </DialogHeader>
        <form>
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

            <div className="grid grid-cols-4 items-center gap-4">
              <Label htmlFor="desiredCurrency" className="text-right">
                Waluta
              </Label>
              <Select defaultValue="PLN">
                <SelectTrigger id="desiredCurrency" className="col-span-3">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="PLN">PLN</SelectItem>
                </SelectContent>
              </Select>
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

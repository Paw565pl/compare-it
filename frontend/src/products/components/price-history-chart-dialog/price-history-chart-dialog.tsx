"use client";

import { Button } from "@/core/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/core/components/ui/dialog";
import { ChartNoAxesCombined } from "lucide-react";

export const PriceHistoryChartDialog = () => {
  return (
    <Dialog>
      <DialogTrigger asChild>
        <Button variant="invisible" size="noPadding">
          <ChartNoAxesCombined />
          <span>Pokaż historię cen</span>
        </Button>
      </DialogTrigger>

      <DialogContent>
        <DialogHeader>
          <DialogTitle>Historia cen</DialogTitle>
          <DialogDescription>
            Na wykresie możesz łatwo prześledzieć zmiany cen tego produktu w
            czasie.
          </DialogDescription>
        </DialogHeader>

        <p>chart</p>
      </DialogContent>
    </Dialog>
  );
};

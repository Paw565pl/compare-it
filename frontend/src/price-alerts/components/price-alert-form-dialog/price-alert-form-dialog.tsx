"use client";

import { Button } from "@/core/components/ui/button";
import { Checkbox } from "@/core/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/core/components/ui/dialog";
import {
  Form,
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/core/components/ui/form";
import { Input } from "@/core/components/ui/input";
import {
  PriceAlertFormValues,
  priceAlertSchema,
} from "@/price-alerts/schemas/price-alert-schema";
import { zodResolver } from "@hookform/resolvers/zod";
import { ReactNode, useState } from "react";
import { useForm } from "react-hook-form";

interface PriceAlertFormDialogProps {
  trigger: ReactNode;
  dialogHeader: string;
  handleSubmit: (formValues: PriceAlertFormValues) => void;
  defaultValues?: PriceAlertFormValues;
}

export const PriceAlertFormDialog = ({
  trigger,
  dialogHeader,
  handleSubmit,
  defaultValues,
}: PriceAlertFormDialogProps) => {
  const [isDialogOpen, setIsDialogOpen] = useState(false);

  const form = useForm<PriceAlertFormValues>({
    resolver: zodResolver(priceAlertSchema),
    defaultValues: {
      targetPrice: defaultValues?.targetPrice.toString() ?? "",
      isOutletAllowed: defaultValues?.isOutletAllowed ?? false,
    },
  });

  const handleFormSubmit = (formValues: PriceAlertFormValues) => {
    form.reset();
    setIsDialogOpen(false);

    handleSubmit(formValues);
  };

  return (
    <Dialog open={isDialogOpen} onOpenChange={setIsDialogOpen}>
      <DialogTrigger asChild>{trigger}</DialogTrigger>

      <DialogContent className="sm:max-w-110">
        <DialogHeader>
          <DialogTitle>{dialogHeader}</DialogTitle>
          <DialogDescription>
            Wypełnij informacje potrzebne, aby utworzyć lub zaktualizować alert
            cenowy.
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form
            onSubmit={form.handleSubmit(handleFormSubmit)}
            className="flex flex-col justify-end gap-4 py-4"
          >
            <FormField
              control={form.control}
              name="targetPrice"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Cena docelowa (PLN)</FormLabel>
                  <FormControl>
                    <Input {...field} />
                  </FormControl>

                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="isOutletAllowed"
              render={({ field }) => (
                <FormItem>
                  <div className="flex items-center gap-2">
                    <FormControl>
                      <Checkbox
                        checked={field.value}
                        onCheckedChange={field.onChange}
                      />
                    </FormControl>
                    <FormLabel className="cursor-pointer">
                      Uwzględnić produkty z outletu
                    </FormLabel>
                  </div>

                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button type="submit" variant="primary">
                ZAPISZ
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};

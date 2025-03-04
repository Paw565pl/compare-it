"use client";

import { Button } from "@/core/components/ui/button";
import { Checkbox } from "@/core/components/ui/checkbox";
import {
  Dialog,
  DialogContent,
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
import { ReactNode, useRef } from "react";
import { useForm } from "react-hook-form";

interface PriceAlertFormDialogProps {
  dialogTrigger: ReactNode;
  dialogHeader: string;
  handleSubmit: (formValues: PriceAlertFormValues) => void;
  defaultValues?: PriceAlertFormValues;
}

export const PriceAlertFormDialog = ({
  dialogTrigger,
  dialogHeader,
  handleSubmit,
  defaultValues,
}: PriceAlertFormDialogProps) => {
  const dialogTriggerRef = useRef<HTMLButtonElement>(null);

  const form = useForm<PriceAlertFormValues>({
    resolver: zodResolver(priceAlertSchema),
    defaultValues: {
      targetPrice: defaultValues?.targetPrice.toString() ?? "",
      isOutletAllowed: defaultValues?.isOutletAllowed ?? false,
    },
  });

  const handleFormSubmit = (formValues: PriceAlertFormValues) => {
    handleSubmit(formValues);
    dialogTriggerRef.current?.click();
  };

  return (
    <Dialog>
      <DialogTrigger asChild ref={dialogTriggerRef}>
        {dialogTrigger}
      </DialogTrigger>

      <DialogContent className="sm:max-w-[425px]">
        <DialogHeader>
          <DialogTitle>{dialogHeader}</DialogTitle>
        </DialogHeader>

        <Form {...form}>
          <form
            // eslint-disable-next-line react-compiler/react-compiler
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
                    <Input className="focus:border-secondary" {...field} />
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
                        className="border-secondary data-[state=checked]:bg-secondary data-[state=checked]:text-secondary-foreground"
                      />
                    </FormControl>
                    <FormLabel className="mt-0!">
                      Uwzględnić produkty z outletu
                    </FormLabel>
                  </div>

                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <Button type="submit" variant="secondary">
                Zapisz
              </Button>
            </DialogFooter>
          </form>
        </Form>
      </DialogContent>
    </Dialog>
  );
};

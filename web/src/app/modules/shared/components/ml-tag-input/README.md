# {TITLE} (Component)

   Tag InputThis component is a text input box that automatically creates tags out of a typed text.

## Inputs

| Property                    | Type                                          | Req   | Description                                                                                                           | Default |
| --------------------------- | --------------------------------------------- | ----- | --------------------------------------------------------------------------------------------------------------------- | ------- |
| label                       | String \| null                                | False | Label to show over the input. It is empty by default                                                                  | null    |
| tooltip                     | String \| null                                | False | The tooltip to show  over the label. It is empty by default                                                           | null    |
| placeholder                 | String \| null                                | False | The text that appears as placeholder of the input. It is empty by default                                             | null    |
| errorMessage                | String \| null                                | False | Error message to show. It is empty by default                                                                         | null    |
| type                        | String \| null                                | False | Type of the items                                                                                                     | null    |
| withAutocomplete            | Boolean                                       | False | Enable autocomplete feature. It is false by default                                                                   | false   |
| autocompleteList            | (StDropDownMenuItem \| StDropDownMenuGroup)[] | False | List to be used for autocomplete feature. It is empty by default                                                      | Array() |
| charsToShowAutocompleteList | Boolean                                       | False | List to be used for autocomplete feature. It is empty by default                                                      | Array() |
| allowFreeText               | Boolean                                       | False | Boolean to allow user to type a free text or not                                                                      | true    |
| infoMessage                 | String                                        | False | Message used to inform user about what values he has to introduce                                                     |         |
| forbiddenValues             | String[]                                      | False | A list of values that user can not type and if he types one of them,tag input will be invalid. It is empty by default | Array() |
| regularExpression           | String                                        | False | Regular expression to validate values. It is null by default                                                          |         |
| forceValidations            | Boolean                                       | False | If you specify it to 'true', the tag input checks the errors before being modified by user                            | false   |
| disabled                    | Boolean                                       | False | Disable the component. It is false by default                                                                         | false   |

## Example


```html
<st-tag-input class="st-form-field"
      name="tag-input-reactive"
      formControlName="tag-input-reactive"
      [autocompleteList]="filteredlist"
      [withAutocomplete]="true"
      [disabled]="disabled"
      [label]="'Tag Input with Reactive Form'"
      [id]="'tag-input-reactive'"
      [placeholder]="'Add tags separated by commas'"
      [tooltip]="'This is a Tag Input component tooltip'"
      [forbiddenValues]="['test']"
      (input)="onFilterList($event)">
</st-tag-input>
<st-tag-input class="st-form-field"
      name="tag-input-template-driven"
      [(ngModel)]="tags.templateDriven"
      [autocompleteList]="filteredlist"
      [withAutocomplete]="true"
      [disabled]="disabled"
      [label]="'Tag Input with Template Driven Form'"
      [id]="'tag-input-template-driven'"
      [placeholder]="'Add tags separated by commas'"
      [tooltip]="'This is a Tag Input component tooltip'"
      [regularExpression]="pattern"
      (input)="onFilterList($event)">
</st-tag-input>
```

